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
import generated.*
import generators.Generators
import models.Index
import models.reference.Incident
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.incident.{IncidentCodePage, IncidentTextPage}
import pages.sections.incidents.{IncidentEndorsementSection, IncidentLocationSection, IncidentSection, TranshipmentSection}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import services.ReferenceDataService

import scala.concurrent.Future

class IncidentsTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[IncidentsTransformer]

  private lazy val mockIncidentEndorsementTransformer: IncidentEndorsementTransformer = mock[IncidentEndorsementTransformer]
  private lazy val mockIncidentLocationTransformer: IncidentLocationTransformer       = mock[IncidentLocationTransformer]
  private lazy val mockTranshipmentTransformer: TranshipmentTransformer               = mock[TranshipmentTransformer]

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataService].toInstance(mockReferenceDataService),
        bind[IncidentEndorsementTransformer].toInstance(mockIncidentEndorsementTransformer),
        bind[IncidentLocationTransformer].toInstance(mockIncidentLocationTransformer),
        bind[TranshipmentTransformer].toInstance(mockTranshipmentTransformer)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockIncidentEndorsementTransformer)
    reset(mockIncidentLocationTransformer)
    reset(mockTranshipmentTransformer)
  }

  // Because each incident has its own set of mocks, we need to ensure the values are unique
  private val incidentsGen = arbitrary[Seq[IncidentType03]]
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
              when(mockReferenceDataService.getIncidentType(eqTo(type0.code))(any()))
                .thenReturn(
                  Future.successful(Incident(type0.code, i.toString))
                )

              when(mockIncidentEndorsementTransformer.transform(any(), eqTo(Index(i)))(any()))
                .thenReturn {
                  ua => Future.successful(ua.setValue(IncidentEndorsementSection(Index(i)), Json.obj("foo" -> i.toString)))
                }

              when(mockIncidentLocationTransformer.transform(any(), eqTo(Index(i)))(any()))
                .thenReturn {
                  ua => Future.successful(ua.setValue(IncidentLocationSection(Index(i)), Json.obj("foo" -> i.toString)))
                }

              when(mockTranshipmentTransformer.transform(any(), eqTo(Index(i)))(any()))
                .thenReturn {
                  ua => Future.successful(ua.setValue(TranshipmentSection(Index(i)), Json.obj("foo" -> i.toString)))
                }
          }

          val result = transformer.transform(incidents).apply(emptyUserAnswers).futureValue

          incidents.zipWithIndex.map {
            case (incident, i) =>
              result.getSequenceNumber(IncidentSection(Index(i))) mustEqual incident.sequenceNumber
              result.getValue(IncidentCodePage(Index(i))).code mustEqual incident.code
              result.getValue(IncidentCodePage(Index(i))).description mustEqual i.toString
              result.getValue(IncidentTextPage(Index(i))) mustEqual incident.text
              result.getValue(IncidentEndorsementSection(Index(i))) mustEqual Json.obj("foo" -> i.toString)
              result.getValue(IncidentLocationSection(Index(i))) mustEqual Json.obj("foo" -> i.toString)
              result.getValue(TranshipmentSection(Index(i))) mustEqual Json.obj("foo" -> i.toString)
          }
      }
    }
  }
}
