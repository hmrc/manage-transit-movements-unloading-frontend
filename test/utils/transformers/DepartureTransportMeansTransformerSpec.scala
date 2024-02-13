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
import generated.DepartureTransportMeansType02
import generators.Generators
import models.Index
import models.departureTransportMeans.TransportMeansIdentification
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import services.MeansOfTransportIdentificationTypesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureTransportMeansTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockTransportIdentificationTypesService: MeansOfTransportIdentificationTypesService = mock[MeansOfTransportIdentificationTypesService]

  private val transformer = new DepartureTransportMeansTransformer(mockTransportIdentificationTypesService)

  override def beforeEach(): Unit =
    reset(mockTransportIdentificationTypesService)

  "must transform data" - {
    "when consignment level" in {
      forAll(arbitrary[Seq[DepartureTransportMeansType02]]) {
        departureTransportMeans =>
          departureTransportMeans.zipWithIndex.map {
            case (dtm, i) =>
              when(mockTransportIdentificationTypesService.getMeansOfTransportIdentificationType(any())(any()))
                .thenReturn(Future.successful(TransportMeansIdentification(dtm.typeOfIdentification, "description")))

              val result = transformer.transform(departureTransportMeans)(hc).apply(emptyUserAnswers).futureValue

              result.getValue(TransportMeansIdentificationPage(Index(i))).code mustBe dtm.typeOfIdentification
              result.getValue(VehicleIdentificationNumberPage(Index(i))) mustBe dtm.identificationNumber
              result.getValue(CountryPage(Index(i))) mustBe dtm.nationality
          }
      }
    }

    "when house consignment level" in {
      forAll(arbitrary[Seq[DepartureTransportMeansType02]]) {
        departureTransportMeans =>
          departureTransportMeans.zipWithIndex.map {
            case (dtm, i) =>
              val result = transformer.transform(departureTransportMeans, hcIndex).apply(emptyUserAnswers).futureValue

              result.getValue(DepartureTransportMeansIdentificationTypePage(hcIndex, Index(i))) mustBe dtm.typeOfIdentification
              result.getValue(DepartureTransportMeansIdentificationNumberPage(hcIndex, Index(i))) mustBe dtm.identificationNumber
              result.getValue(DepartureTransportMeansCountryPage(hcIndex, Index(i))) mustBe dtm.nationality
          }
      }
    }
  }
}
