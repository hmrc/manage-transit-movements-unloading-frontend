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
import generated.{CUSTOM_DepartureTransportMeansType01, DepartureTransportMeansType01}
import generators.Generators
import models.Index
import models.reference.{Country, TransportMeansIdentification}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService

import scala.concurrent.Future

class DepartureTransportMeansTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataService].toInstance(mockReferenceDataService)
      )

  private val transformer = app.injector.instanceOf[DepartureTransportMeansTransformer]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  "must transform data" - {
    "when consignment level" - {
      import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
      import pages.sections.TransportMeansSection

      def departureTransportMeansGen(implicit a: Arbitrary[CUSTOM_DepartureTransportMeansType01]): Gen[Seq[CUSTOM_DepartureTransportMeansType01]] =
        listWithMaxLength[CUSTOM_DepartureTransportMeansType01]()(a)
          .map {
            _.distinctBy(_.typeOfIdentification)
              .distinctBy(_.nationality)
          }

      "when values defined" in {
        forAll(departureTransportMeansGen(arbitraryCUSTOM_DepartureTransportMeansType01AllDefined)) {
          departureTransportMeans =>
            beforeEach()

            departureTransportMeans.zipWithIndex.map {
              case (dtm, i) =>
                when(mockReferenceDataService.getMeansOfTransportIdentificationType(eqTo(dtm.typeOfIdentification.value))(any()))
                  .thenReturn(Future.successful(TransportMeansIdentification(dtm.typeOfIdentification.value, i.toString)))

                when(mockReferenceDataService.getCountry(eqTo(dtm.nationality.value))(any()))
                  .thenReturn(Future.successful(Country(dtm.nationality.value, i.toString)))
            }

            val result = transformer.transform(departureTransportMeans).apply(emptyUserAnswers).futureValue

            departureTransportMeans.zipWithIndex.map {
              case (dtm, i) =>
                val dtmIndex = Index(i)

                result.getSequenceNumber(TransportMeansSection(dtmIndex)) mustEqual dtm.sequenceNumber
                result.getValue(TransportMeansIdentificationPage(dtmIndex)).code mustEqual dtm.typeOfIdentification.value
                result.getValue(TransportMeansIdentificationPage(dtmIndex)).description mustEqual i.toString
                result.getValue(VehicleIdentificationNumberPage(dtmIndex)) mustEqual dtm.identificationNumber.value
                result.getValue(CountryPage(dtmIndex)).code mustEqual dtm.nationality.value
                result.getValue(CountryPage(dtmIndex)).description mustEqual i.toString
            }
        }
      }

      "when no values defined" in {
        forAll(departureTransportMeansGen(arbitraryCUSTOM_DepartureTransportMeansType01NoneDefined)) {
          departureTransportMeans =>
            beforeEach()

            val result = transformer.transform(departureTransportMeans).apply(emptyUserAnswers).futureValue

            departureTransportMeans.zipWithIndex.map {
              case (dtm, i) =>
                val dtmIndex = Index(i)

                result.getSequenceNumber(TransportMeansSection(dtmIndex)) mustEqual dtm.sequenceNumber
                result.get(TransportMeansIdentificationPage(dtmIndex)) must not be defined
                result.get(VehicleIdentificationNumberPage(dtmIndex)) must not be defined
                result.get(CountryPage(dtmIndex)) must not be defined
            }
        }
      }
    }

    "when house consignment level" in {
      import pages.houseConsignment.index.departureMeansOfTransport.*
      import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansSection

      def departureTransportMeansGen(implicit a: Arbitrary[DepartureTransportMeansType01]): Gen[Seq[DepartureTransportMeansType01]] =
        listWithMaxLength[DepartureTransportMeansType01]()(a)
          .map {
            _.distinctBy(_.typeOfIdentification)
              .distinctBy(_.nationality)
          }

      forAll(departureTransportMeansGen(arbitraryDepartureTransportMeansType01)) {
        departureTransportMeans =>
          departureTransportMeans.zipWithIndex.map {
            case (dtm, i) =>
              when(mockReferenceDataService.getMeansOfTransportIdentificationType(any())(any()))
                .thenReturn(Future.successful(TransportMeansIdentification(dtm.typeOfIdentification, i.toString)))

              when(mockReferenceDataService.getCountry(any())(any()))
                .thenReturn(Future.successful(Country(dtm.nationality, i.toString)))

              val result = transformer.transform(departureTransportMeans, hcIndex).apply(emptyUserAnswers).futureValue

              val dtmIndex = Index(i)

              result.getSequenceNumber(TransportMeansSection(hcIndex, dtmIndex)) mustEqual dtm.sequenceNumber
              result.getValue(TransportMeansIdentificationPage(hcIndex, dtmIndex)).code mustEqual dtm.typeOfIdentification
              result.getValue(TransportMeansIdentificationPage(hcIndex, dtmIndex)).description mustEqual i.toString
              result.getValue(VehicleIdentificationNumberPage(hcIndex, dtmIndex)) mustEqual dtm.identificationNumber
              result.getValue(CountryPage(hcIndex, dtmIndex)).code mustEqual dtm.nationality
              result.getValue(CountryPage(hcIndex, dtmIndex)).description mustEqual i.toString
          }
      }
    }
  }
}
