/*
 * Copyright 2023 HM Revenue & Customs
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

package viewModels.houseConsignment

import base.SpecBase
import generators.Generators
import models.reference.{Country, TransportMeansIdentification}
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index._
import pages.houseConsignment.index.departureMeansOfTransport._
import pages.sections.HouseConsignmentSection
import viewModels.ListItem
import viewModels.houseConsignment.AddAnotherHouseConsignmentViewModel.AddAnotherHouseConsignmentViewModelProvider

class AddAnotherHouseConsignmentViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AddAnotherHouseConsignmentViewModelSpec" - {
    "list items" - {

      "must get list items" - {

        "when there is one house consignment" in {
          forAll(arbitrary[Mode], arbitrary[BigDecimal], arbitrary[TransportMeansIdentification], nonEmptyString.sample.value, arbitrary[Country]) {
            (mode, weight, identification, text, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(GrossWeightPage(Index(0)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(0)), true)
                .setValue(TransportMeansIdentificationPage(Index(0), Index(0)), identification)
                .setValue(VehicleIdentificationNumberPage(Index(0), Index(0)), text)
                .setValue(CountryPage(Index(0), Index(0)), country)

              val result = new AddAnotherHouseConsignmentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems.length mustBe 1
              result.title mustBe "You have added 1 house consignment"
              result.heading mustBe "You have added 1 house consignment"
              result.legend mustBe "Do you want to add another house consignment?"
              result.maxLimitLabel mustBe "You cannot add any more house consignments. To add another, you need to remove one first."
              result.nextIndex mustBe Index(1)
          }
        }

        "when there are multiple house consignments" in {
          forAll(arbitrary[Mode], arbitrary[BigDecimal], arbitrary[TransportMeansIdentification], nonEmptyString.sample.value, arbitrary[Country]) {
            (mode, weight, identification, text, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(GrossWeightPage(Index(0)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(0)), true)
                .setValue(TransportMeansIdentificationPage(Index(0), Index(0)), identification)
                .setValue(VehicleIdentificationNumberPage(Index(0), Index(0)), text)
                .setValue(CountryPage(Index(0), Index(0)), country)
                .setValue(GrossWeightPage(Index(1)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(1)), false)
                .setValue(GrossWeightPage(Index(2)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(2)), true)
                .setValue(TransportMeansIdentificationPage(Index(2), Index(0)), identification)
                .setValue(VehicleIdentificationNumberPage(Index(2), Index(0)), text)
                .setValue(CountryPage(Index(2), Index(0)), country)
                .setValue(GrossWeightPage(Index(3)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(3)), false)

              val result = new AddAnotherHouseConsignmentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems.length mustBe 4
              result.title mustBe s"You have added 4 house consignments"
              result.heading mustBe s"You have added 4 house consignments"
              result.legend mustBe "Do you want to add another house consignment?"
              result.maxLimitLabel mustBe "You cannot add any more house consignments. To add another, you need to remove one first."
              result.nextIndex mustBe Index(4)
          }
        }

        "when one has been removed" in {
          forAll(arbitrary[Mode], arbitrary[BigDecimal], arbitrary[TransportMeansIdentification], nonEmptyString.sample.value, arbitrary[Country]) {
            (mode, weight, identification, text, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(GrossWeightPage(Index(0)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(0)), true)
                .setValue(TransportMeansIdentificationPage(Index(0), Index(0)), identification)
                .setValue(VehicleIdentificationNumberPage(Index(0), Index(0)), text)
                .setValue(CountryPage(Index(0), Index(0)), country)
                .setRemoved(HouseConsignmentSection(Index(1)))
                .setValue(GrossWeightPage(Index(2)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(2)), true)
                .setValue(TransportMeansIdentificationPage(Index(2), Index(0)), identification)
                .setValue(VehicleIdentificationNumberPage(Index(2), Index(0)), text)
                .setValue(CountryPage(Index(2), Index(0)), country)
                .setValue(GrossWeightPage(Index(3)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(3)), false)

              val result = new AddAnotherHouseConsignmentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems.length mustBe 3
              result.title mustBe s"You have added 3 house consignments"
              result.heading mustBe s"You have added 3 house consignments"
              result.legend mustBe "Do you want to add another house consignment?"
              result.maxLimitLabel mustBe "You cannot add any more house consignments. To add another, you need to remove one first."
              result.nextIndex mustBe Index(4)
          }
        }

        "and show remove links" in {
          forAll(arbitrary[Mode], arbitrary[BigDecimal], arbitrary[TransportMeansIdentification], nonEmptyString.sample.value, arbitrary[Country]) {
            (mode, weight, identification, text, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(GrossWeightPage(Index(0)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(0)), true)
                .setValue(TransportMeansIdentificationPage(Index(0), Index(0)), identification)
                .setValue(VehicleIdentificationNumberPage(Index(0), Index(0)), text)
                .setValue(CountryPage(Index(0), Index(0)), country)
                .setValue(GrossWeightPage(Index(1)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(1)), false)
                .setValue(GrossWeightPage(Index(2)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(2)), true)
                .setValue(TransportMeansIdentificationPage(Index(2), Index(0)), identification)
                .setValue(VehicleIdentificationNumberPage(Index(2), Index(0)), text)
                .setValue(CountryPage(Index(2), Index(0)), country)
                .setValue(GrossWeightPage(Index(3)), weight)
                .setValue(AddDepartureTransportMeansYesNoPage(Index(3)), false)

              val result = new AddAnotherHouseConsignmentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = s"House consignment 1",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.houseConsignment.index.routes.RemoveHouseConsignmentYesNoController
                      .onPageLoad(arrivalId, Index(0), mode)
                      .url
                  )
                ),
                ListItem(
                  name = s"House consignment 2",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.houseConsignment.index.routes.RemoveHouseConsignmentYesNoController
                      .onPageLoad(arrivalId, Index(1), mode)
                      .url
                  )
                ),
                ListItem(
                  name = s"House consignment 3",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.houseConsignment.index.routes.RemoveHouseConsignmentYesNoController
                      .onPageLoad(arrivalId, Index(2), mode)
                      .url
                  )
                ),
                ListItem(
                  name = s"House consignment 4",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.houseConsignment.index.routes.RemoveHouseConsignmentYesNoController
                      .onPageLoad(arrivalId, Index(3), mode)
                      .url
                  )
                )
              )

              result.nextIndex mustBe Index(4)
          }
        }
      }
    }
  }
}
