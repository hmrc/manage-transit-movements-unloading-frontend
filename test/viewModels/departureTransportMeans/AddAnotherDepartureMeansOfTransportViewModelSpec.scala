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

package viewModels.departureTransportMeans

import base.SpecBase
import generators.Generators
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.Country
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.departureMeansOfTransport._
import viewModels.ListItem
import viewModels.departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel.AddAnotherDepartureMeansOfTransportViewModelProvider

class AddAnotherDepartureMeansOfTransportViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AddAnotherDepartureMeansOfTransportViewModelSpec" - {
    "list items" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val result = new AddAnotherDepartureMeansOfTransportViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems mustBe Nil
              result.title mustBe "You have added 0 departure means of transport"
              result.heading mustBe "You have added 0 departure means of transport"
              result.legend mustBe "Do you want to add a departure means of transport?"
              result.maxLimitLabel mustBe "You cannot add another departure means of transport. To add another, you need to remove one first."
          }
        }
      }

      "must get list items" - {

        "when there is one departure means of transport" in {
          forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification]) {
            (mode, identification) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransportMeansIdentificationPage(Index(0)), identification)

              val result = new AddAnotherDepartureMeansOfTransportViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems.length mustBe 1
              result.title mustBe "You have added 1 departure means of transport"
              result.heading mustBe "You have added 1 departure means of transport"
              result.legend mustBe "Do you want to add another departure means of transport?"
              result.maxLimitLabel mustBe "You cannot add another departure means of transport. To add another, you need to remove one first."
          }
        }

        "when there are multiple departure means of transport" in {
          forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], Gen.alphaStr, arbitrary[Country]) {
            (mode, identification, identificationNumber, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddIdentificationYesNoPage(Index(0)), true)
                .setValue(TransportMeansIdentificationPage(Index(0)), identification)
                .setValue(AddIdentificationNumberYesNoPage(Index(0)), false)
                .setValue(AddNationalityYesNoPage(Index(0)), true)
                .setValue(CountryPage(Index(0)), country)
                .setValue(AddIdentificationYesNoPage(Index(1)), true)
                .setValue(TransportMeansIdentificationPage(Index(1)), identification)
                .setValue(AddIdentificationNumberYesNoPage(Index(1)), true)
                .setValue(VehicleIdentificationNumberPage(Index(1)), identificationNumber)
                .setValue(AddNationalityYesNoPage(Index(1)), false)
                .setValue(AddIdentificationYesNoPage(Index(2)), false)
                .setValue(AddIdentificationNumberYesNoPage(Index(2)), true)
                .setValue(VehicleIdentificationNumberPage(Index(2)), identificationNumber)
                .setValue(AddNationalityYesNoPage(Index(2)), false)
                .setValue(AddIdentificationYesNoPage(Index(3)), false)
                .setValue(AddIdentificationNumberYesNoPage(Index(3)), false)
                .setValue(AddNationalityYesNoPage(Index(3)), false)

              val result = new AddAnotherDepartureMeansOfTransportViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems.length mustBe 4
              result.title mustBe s"You have added 4 departure means of transport"
              result.heading mustBe s"You have added 4 departure means of transport"
              result.legend mustBe "Do you want to add another departure means of transport?"
              result.maxLimitLabel mustBe "You cannot add another departure means of transport. To add another, you need to remove one first."
          }
        }

        "and show change and remove links" in {
          forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], Gen.alphaStr, arbitrary[Country]) {
            (mode, identification, identificationNumber, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddIdentificationYesNoPage(Index(0)), true)
                .setValue(TransportMeansIdentificationPage(Index(0)), identification)
                .setValue(AddIdentificationNumberYesNoPage(Index(0)), true)
                .setValue(VehicleIdentificationNumberPage(Index(0)), identificationNumber)
                .setValue(AddNationalityYesNoPage(Index(0)), true)
                .setValue(CountryPage(Index(0)), country)
                .setValue(AddIdentificationYesNoPage(Index(1)), false)
                .setValue(AddIdentificationNumberYesNoPage(Index(1)), false)
                .setValue(AddNationalityYesNoPage(Index(1)), false)

              val result = new AddAnotherDepartureMeansOfTransportViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = s"Departure means of transport 1 - $identification - $identificationNumber",
                  changeUrl = Some("#"), //TODO: To be added later
                  removeUrl = Some("#") //TODO: To be added later
                ),
                ListItem(
                  name = s"Departure means of transport 2",
                  changeUrl = Some("#"), //TODO: To be added later
                  removeUrl = Some("#") //TODO: To be added later
                )
              )
          }
        }
      }
    }
  }
}
