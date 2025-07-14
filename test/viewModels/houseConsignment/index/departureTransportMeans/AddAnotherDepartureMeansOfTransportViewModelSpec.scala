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

package viewModels.houseConsignment.index.departureTransportMeans

import base.SpecBase
import generators.Generators
import models.reference.{Country, TransportMeansIdentification}
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import viewModels.ListItem
import viewModels.houseConsignment.index.departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel.AddAnotherDepartureMeansOfTransportViewModelProvider

class AddAnotherDepartureMeansOfTransportViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AddAnotherDepartureMeansOfTransportViewModelSpec" - {
    "list items" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val result = new AddAnotherDepartureMeansOfTransportViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, mode)
              result.listItems mustEqual Nil
              result.title mustEqual "You have added 0 departure means of transport for house consignment 1"
              result.heading mustEqual "You have added 0 departure means of transport for house consignment 1"
              result.legend mustEqual "Do you want to add a departure means of transport for house consignment 1?"
              result.maxLimitLabel mustEqual "You cannot add another departure means of transport for house consignment 1. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(0)
          }
        }
      }

      "must get list items" - {

        "when there is one departure means of transport" in {
          forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification]) {
            (mode, identification) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransportMeansIdentificationPage(houseConsignmentIndex, Index(0)), identification)

              val result = new AddAnotherDepartureMeansOfTransportViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, mode)

              result.listItems.length mustEqual 1
              result.title mustEqual "You have added 1 departure means of transport for house consignment 1"
              result.heading mustEqual "You have added 1 departure means of transport for house consignment 1"
              result.legend mustEqual "Do you want to add another departure means of transport for house consignment 1?"
              result.maxLimitLabel mustEqual "You cannot add another departure means of transport for house consignment 1. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(1)
          }
        }

        "when there are multiple departure means of transport" in {
          forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], Gen.alphaStr, arbitrary[Country]) {
            (mode, identification, identificationNumber, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransportMeansIdentificationPage(houseConsignmentIndex, Index(0)), identification)
                .setValue(TransportMeansIdentificationPage(houseConsignmentIndex, Index(1)), identification)
                .setValue(TransportMeansIdentificationPage(houseConsignmentIndex, Index(2)), identification)
                .setValue(CountryPage(houseConsignmentIndex, Index(0)), country)
                .setValue(CountryPage(houseConsignmentIndex, Index(1)), country)
                .setValue(CountryPage(houseConsignmentIndex, Index(2)), country)
                .setValue(VehicleIdentificationNumberPage(houseConsignmentIndex, Index(0)), identificationNumber)
                .setValue(VehicleIdentificationNumberPage(houseConsignmentIndex, Index(1)), identificationNumber)
                .setValue(VehicleIdentificationNumberPage(houseConsignmentIndex, Index(2)), identificationNumber)

              val result = new AddAnotherDepartureMeansOfTransportViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, mode)
              result.listItems.length mustEqual 3
              result.title mustEqual s"You have added 3 departure means of transport for house consignment 1"
              result.heading mustEqual s"You have added 3 departure means of transport for house consignment 1"
              result.legend mustEqual "Do you want to add another departure means of transport for house consignment 1?"
              result.maxLimitLabel mustEqual "You cannot add another departure means of transport for house consignment 1. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(3)
          }
        }

        "and show remove links" in {
          forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], Gen.alphaStr, arbitrary[Country]) {
            (mode, identification, identificationNumber, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransportMeansIdentificationPage(houseConsignmentIndex, Index(0)), identification)
                .setValue(VehicleIdentificationNumberPage(houseConsignmentIndex, Index(0)), identificationNumber)
                .setValue(CountryPage(houseConsignmentIndex, Index(0)), country)

              val result = new AddAnotherDepartureMeansOfTransportViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, mode)

              result.listItems mustEqual Seq(
                ListItem(
                  name = s"Departure means of transport 1 - $identification - $identificationNumber",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.houseConsignment.index.departureMeansOfTransport.routes.RemoveDepartureMeansOfTransportYesNoController
                      .onPageLoad(arrivalId, houseConsignmentIndex, Index(0), mode)
                      .url
                  )
                )
              )

              result.nextIndex mustEqual Index(1)
          }
        }
      }
    }
  }
}
