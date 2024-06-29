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
import generated.{DepartureTransportMeansType02, DepartureTransportMeansType07}
import generators.Generators
import models.Index
import models.reference.{Country, TransportMeansIdentification}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
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
  private val departureTransportMeansType02Gen = arbitrary[Seq[DepartureTransportMeansType02]]
    .map {
      _.distinctBy(_.typeOfIdentification)
        .distinctBy(_.nationality)
    }

  private def departureTransportMeansType07Gen(implicit a: Arbitrary[DepartureTransportMeansType07]): Gen[Seq[DepartureTransportMeansType07]] =
    arbitrary[Seq[DepartureTransportMeansType07]]
      .map {
        _.distinctBy(_.typeOfIdentification)
          .distinctBy(_.nationality)
      }

  "must transform data" - {
    "when consignment level" - {
      import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
      import pages.sections.TransportMeansSection

      "when values defined" in {
        forAll(departureTransportMeansType07Gen(arbitraryDepartureTransportMeansType07AllDefined)) {
          departureTransportMeans =>
            beforeEach()

            departureTransportMeans.zipWithIndex.map {
              case (dtm, i) =>
                when(mockReferenceDataConnector.getMeansOfTransportIdentificationType(eqTo(dtm.typeOfIdentification.value))(any(), any()))
                  .thenReturn(Future.successful(TransportMeansIdentification(dtm.typeOfIdentification.value, i.toString)))

                when(mockReferenceDataConnector.getCountry(eqTo(dtm.nationality.value))(any(), any()))
                  .thenReturn(Future.successful(Country(dtm.nationality.value, i.toString)))
            }

            val result = transformer.transform(departureTransportMeans).apply(emptyUserAnswers).futureValue

            departureTransportMeans.zipWithIndex.map {
              case (dtm, i) =>
                val dtmIndex = Index(i)

                result.getSequenceNumber(TransportMeansSection(dtmIndex)) mustBe dtm.sequenceNumber
                result.getValue(TransportMeansIdentificationPage(dtmIndex)).code mustBe dtm.typeOfIdentification.value
                result.getValue(TransportMeansIdentificationPage(dtmIndex)).description mustBe i.toString
                result.getValue(VehicleIdentificationNumberPage(dtmIndex)) mustBe dtm.identificationNumber.value
                result.getValue(CountryPage(dtmIndex)).code mustBe dtm.nationality.value
            }
        }
      }

      "when no values defined" in {
        forAll(departureTransportMeansType07Gen(arbitraryDepartureTransportMeansType07NoneDefined)) {
          departureTransportMeans =>
            beforeEach()

            val result = transformer.transform(departureTransportMeans).apply(emptyUserAnswers).futureValue

            departureTransportMeans.zipWithIndex.map {
              case (dtm, i) =>
                val dtmIndex = Index(i)

                result.getSequenceNumber(TransportMeansSection(dtmIndex)) mustBe dtm.sequenceNumber
                result.get(TransportMeansIdentificationPage(dtmIndex)) must not be defined
                result.get(VehicleIdentificationNumberPage(dtmIndex)) must not be defined
                result.get(CountryPage(dtmIndex)) must not be defined
            }
        }
      }
    }

    "when house consignment level" in {
      import pages.houseConsignment.index.departureMeansOfTransport._
      import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansSection

      forAll(departureTransportMeansType02Gen) {
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
              result.getValue(TransportMeansIdentificationPage(hcIndex, dtmIndex)).description mustBe dtm.typeOfIdentification
              result.getValue(VehicleIdentificationNumberPage(hcIndex, dtmIndex)) mustBe dtm.identificationNumber
              result.getValue(CountryPage(hcIndex, dtmIndex)).description mustBe dtm.nationality
          }
      }
    }
  }
}
