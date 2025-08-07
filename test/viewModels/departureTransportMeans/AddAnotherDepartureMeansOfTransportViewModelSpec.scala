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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.{Country, TransportMeansIdentification}
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.departureMeansOfTransport.*
import pages.sections.TransportMeansSection
import viewModels.ListItem
import viewModels.departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel.AddAnotherDepartureMeansOfTransportViewModelProvider

class AddAnotherDepartureMeansOfTransportViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  "AddAnotherDepartureMeansOfTransportViewModelSpec" - {
    "list items" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val result = new AddAnotherDepartureMeansOfTransportViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems mustEqual Nil
              result.title mustEqual "You have added 0 departure means of transport"
              result.heading mustEqual "You have added 0 departure means of transport"
              result.legend mustEqual "Do you want to add a departure means of transport?"
              result.maxLimitLabel mustEqual "You cannot add another departure means of transport. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(0)
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

              result.listItems.length mustEqual 1
              result.title mustEqual "You have added 1 departure means of transport"
              result.heading mustEqual "You have added 1 departure means of transport"
              result.legend mustEqual "Do you want to add another departure means of transport?"
              result.maxLimitLabel mustEqual "You cannot add another departure means of transport. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(1)
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
              result.listItems.length mustEqual 4
              result.title mustEqual s"You have added 4 departure means of transport"
              result.heading mustEqual s"You have added 4 departure means of transport"
              result.legend mustEqual "Do you want to add another departure means of transport?"
              result.maxLimitLabel mustEqual "You cannot add another departure means of transport. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(4)
          }
        }

        "when one has been removed" in {
          forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], Gen.alphaStr, arbitrary[Country]) {
            (mode, identification, identificationNumber, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddIdentificationYesNoPage(Index(0)), true)
                .setValue(TransportMeansIdentificationPage(Index(0)), identification)
                .setValue(AddIdentificationNumberYesNoPage(Index(0)), false)
                .setValue(AddNationalityYesNoPage(Index(0)), true)
                .setValue(CountryPage(Index(0)), country)
                .setRemoved(TransportMeansSection(Index(1)))
                .setValue(AddIdentificationYesNoPage(Index(2)), false)
                .setValue(AddIdentificationNumberYesNoPage(Index(2)), true)
                .setValue(VehicleIdentificationNumberPage(Index(2)), identificationNumber)
                .setValue(AddNationalityYesNoPage(Index(2)), false)
                .setValue(AddIdentificationYesNoPage(Index(3)), false)
                .setValue(AddIdentificationNumberYesNoPage(Index(3)), false)
                .setValue(AddNationalityYesNoPage(Index(3)), false)

              val result = new AddAnotherDepartureMeansOfTransportViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems.length mustEqual 3
              result.title mustEqual s"You have added 3 departure means of transport"
              result.heading mustEqual s"You have added 3 departure means of transport"
              result.legend mustEqual "Do you want to add another departure means of transport?"
              result.maxLimitLabel mustEqual "You cannot add another departure means of transport. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(4)
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

              result.listItems mustEqual Seq(
                ListItem(
                  name = s"Departure means of transport 1 - $identification - $identificationNumber",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.departureMeansOfTransport.routes.RemoveDepartureMeansOfTransportYesNoController
                      .onPageLoad(arrivalId, mode, Index(0))
                      .url
                  )
                ),
                ListItem(
                  name = s"Departure means of transport 2",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.departureMeansOfTransport.routes.RemoveDepartureMeansOfTransportYesNoController
                      .onPageLoad(arrivalId, mode, Index(1))
                      .url
                  )
                )
              )

              result.nextIndex mustEqual Index(2)
          }
        }
      }
    }
  }
}
