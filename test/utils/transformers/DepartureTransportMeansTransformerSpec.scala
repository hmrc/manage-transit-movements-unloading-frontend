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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.departureMeansOfTransport.{CountryPage, VehicleIdentificationNumberPage}

class DepartureTransportMeansTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[DepartureTransportMeansTransformer]

  "must transform data" - {
    "when consignment level" in {
      forAll(arbitrary[Seq[DepartureTransportMeansType02]]) {
        departureTransportMeans =>
          departureTransportMeans.zipWithIndex.map {
            case (dtm, i) =>
              val result = transformer.transform(departureTransportMeans).apply(emptyUserAnswers).futureValue

              result.getValue(VehicleIdentificationTypePage(Index(i))) mustBe dtm.typeOfIdentification
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
