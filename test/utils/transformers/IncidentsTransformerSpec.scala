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
import models.reference.{Country, Incident}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.incident.{EndorsementCountryPage, IncidentCodePage, IncidentTextPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class IncidentsTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[IncidentsTransformer]

  private lazy val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataConnector].toInstance(mockRefDataConnector)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRefDataConnector)
  }

  // Because each incident has its own set of mocks, we need to ensure the values are unique
  private val incidentsGen = arbitrary[Seq[IncidentType04]]
    .map {
      _.distinctBy(_.code)
        .distinctBy(_.Endorsement)
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

              type0.Endorsement.map {
                endorse =>
                  when(mockRefDataConnector.getCountry(eqTo(endorse.country))(any(), any()))
                    .thenReturn(
                      Future.successful(Country(endorse.country, i.toString))
                    )
              }
          }

          val result = transformer.transform(incidents).apply(emptyUserAnswers).futureValue

          incidents.zipWithIndex.map {
            case (incident, i) =>
              result.getValue(IncidentCodePage(Index(i))).code mustBe incident.code
              result.getValue(IncidentCodePage(Index(i))).description mustBe i.toString
              result.getValue(IncidentTextPage(Index(i))) mustBe incident.text
              result.get(EndorsementCountryPage(Index(i))).map(_.code) mustBe incident.Endorsement.map(_.country)
              result.get(EndorsementCountryPage(Index(i))).map(_.description) mustBe incident.Endorsement.map(
                _ => i.toString
              )
          }
      }
    }
  }
}