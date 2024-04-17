/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils.transformers

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ReferenceDataConnector
import generated._
import generators.Generators
import models.Index
import models.reference.Incident
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import pages.incident.{IncidentCodePage, IncidentTextPage}
import pages.sections.incidents.IncidentSection
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.Future

class IncidentsTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[IncidentsTransformer]

  private lazy val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  private lazy val mockIncidentEndorsementTransformer: IncidentEndorsementTransformer = mock[IncidentEndorsementTransformer]

  private lazy val mockIncidentLocationTransformer: IncidentLocationTransformer = mock[IncidentLocationTransformer]

  private lazy val mockTranshipmentTransformer: ReplacementMeansOfTransportTransformer = mock[ReplacementMeansOfTransportTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataConnector].toInstance(mockRefDataConnector),
        bind[IncidentEndorsementTransformer].toInstance(mockIncidentEndorsementTransformer),
        bind[IncidentLocationTransformer].toInstance(mockIncidentLocationTransformer),
        bind[ReplacementMeansOfTransportTransformer].toInstance(mockTranshipmentTransformer)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRefDataConnector)
    reset(mockIncidentEndorsementTransformer)
    reset(mockIncidentLocationTransformer)
    reset(mockTranshipmentTransformer)
  }

  private case object FakeIncidentEndorsementSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "incidentEndorsement"
  }

  private case object FakeIncidentLocationSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "incidentLocation"
  }

  private case object FakeTranshipmentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "replacementMeansOfTransport"
  }

  // Because each incident has its own set of mocks, we need to ensure the values are unique
  private val incidentsGen = arbitrary[Seq[IncidentType04]]
    .map {
      _.distinctBy(_.code)
        .distinctBy(_.Endorsement)
        .distinctBy(_.Location)
    }

  "must transform data" - {
    "when incidents defined" in {
      forAll(incidentsGen) {
        incidents =>
          beforeEach()

          incidents.zipWithIndex.map {
            case (type0, i) =>
              when(mockRefDataConnector.getIncidentType(eqTo(type0.code))(any(), any()))
                .thenReturn(
                  Future.successful(Incident(type0.code, i.toString))
                )

              when(mockIncidentEndorsementTransformer.transform(any(), eqTo(Index(i)))(any()))
                .thenReturn {
                  ua => Future.successful(ua.setValue(FakeIncidentEndorsementSection, Json.obj("foo" -> i.toString)))
                }

              when(mockIncidentLocationTransformer.transform(any(), eqTo(Index(i)))(any()))
                .thenReturn {
                  ua => Future.successful(ua.setValue(FakeIncidentLocationSection, Json.obj("foo" -> i.toString)))
                }

              when(mockTranshipmentTransformer.transform(any(), eqTo(Index(i)))(any()))
                .thenReturn {
                  ua => Future.successful(ua.setValue(FakeTranshipmentSection, Json.obj("foo" -> i.toString)))
                }
          }

          val result = transformer.transform(incidents).apply(emptyUserAnswers).futureValue

          incidents.zipWithIndex.map {
            case (incident, i) =>
              result.getSequenceNumber(IncidentSection(Index(i))) mustBe incident.sequenceNumber
              result.getValue(IncidentCodePage(Index(i))).code mustBe incident.code
              result.getValue(IncidentCodePage(Index(i))).description mustBe i.toString
              result.getValue(IncidentTextPage(Index(i))) mustBe incident.text
              result.getValue(FakeIncidentEndorsementSection) mustBe Json.obj("foo" -> i.toString)
              result.getValue(FakeIncidentLocationSection) mustBe Json.obj("foo" -> i.toString)
              result.getValue(FakeTranshipmentSection) mustBe Json.obj("foo" -> i.toString)
          }
      }
    }
  }
}
