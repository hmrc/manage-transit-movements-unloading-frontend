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
import generated.DepartureTransportMeansType02
import generators.Generators
import models.Index
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.Country
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class DepartureTransportMeansTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector)
      )

  private val transformer = app.injector.instanceOf[DepartureTransportMeansTransformer]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataConnector)
  }

  // Because each DTM has its own set of mocks, we need to ensure the values are unique
  private val departureTransportMeansGen = arbitrary[Seq[DepartureTransportMeansType02]]
    .map {
      _.distinctBy(_.typeOfIdentification)
        .distinctBy(_.nationality)
    }

  "must transform data" - {
    "when consignment level" in {
      import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
      import pages.sections.TransportMeansSection

      forAll(departureTransportMeansGen) {
        departureTransportMeans =>
          beforeEach()

          departureTransportMeans.zipWithIndex.map {
            case (dtm, i) =>
              when(mockReferenceDataConnector.getMeansOfTransportIdentificationType(eqTo(dtm.typeOfIdentification))(any(), any()))
                .thenReturn(Future.successful(TransportMeansIdentification(dtm.typeOfIdentification, i.toString)))

              when(mockReferenceDataConnector.getCountry(eqTo(dtm.nationality))(any(), any()))
                .thenReturn(Future.successful(Country(dtm.nationality, i.toString)))
          }

          val result = transformer.transform(departureTransportMeans).apply(emptyUserAnswers).futureValue

          departureTransportMeans.zipWithIndex.map {
            case (dtm, i) =>
              val dtmIndex = Index(i)

              result.getSequenceNumber(TransportMeansSection(dtmIndex)) mustBe dtm.sequenceNumber
              result.getValue(TransportMeansIdentificationPage(dtmIndex)).code mustBe dtm.typeOfIdentification
              result.getValue(TransportMeansIdentificationPage(dtmIndex)).description mustBe i.toString
              result.getValue(VehicleIdentificationNumberPage(dtmIndex)) mustBe dtm.identificationNumber
              result.getValue(CountryPage(dtmIndex)).code mustBe dtm.nationality
          }
      }
    }

    "when house consignment level" in {
      import pages._
      import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansSection

      forAll(departureTransportMeansGen) {
        departureTransportMeans =>
          departureTransportMeans.zipWithIndex.map {
            case (dtm, i) =>
              when(mockReferenceDataConnector.getMeansOfTransportIdentificationType(any())(any(), any()))
                .thenReturn(Future.successful(TransportMeansIdentification(dtm.typeOfIdentification, dtm.typeOfIdentification)))

              when(mockReferenceDataConnector.getCountry(any())(any(), any()))
                .thenReturn(Future.successful(Country(dtm.nationality, dtm.nationality)))

              val result = transformer.transform(departureTransportMeans, hcIndex).apply(emptyUserAnswers).futureValue

              val dtmIndex = Index(i)

              result.getSequenceNumber(TransportMeansSection(hcIndex, dtmIndex)) mustBe dtm.sequenceNumber
              result.getValue(DepartureTransportMeansIdentificationTypePage(hcIndex, dtmIndex)).description mustBe dtm.typeOfIdentification
              result.getValue(DepartureTransportMeansIdentificationNumberPage(hcIndex, dtmIndex)) mustBe dtm.identificationNumber
              result.getValue(DepartureTransportMeansCountryPage(hcIndex, dtmIndex)).description mustBe dtm.nationality
          }
      }
    }
  }
}
