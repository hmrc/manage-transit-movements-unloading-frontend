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

package viewModels.countriesOfRouting

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.countriesOfRouting.routes
import generators.Generators
import models.reference.{Country, TransportMeansIdentification}
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.countriesOfRouting.CountryOfRoutingPage
import pages.sections.CountryOfRoutingSection
import viewModels.ListItem
import viewModels.countriesOfRouting.AddAnotherCountryViewModel.AddAnotherCountryViewModelProvider

class AddAnotherCountryViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  "AddAnotherCountryViewModel" - {
    "list items" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val result = new AddAnotherCountryViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems mustEqual Nil
              result.title mustEqual "You have added 0 countries to the transit route"
              result.heading mustEqual "You have added 0 countries to the transit route"
              result.legend mustEqual "Do you want to add a country to the transit route?"
              result.maxLimitLabel mustEqual "You cannot add another country to the transit route. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(0)
          }
        }
      }

      "must get list items" - {

        "when there is one country" in {
          forAll(arbitrary[Mode], arbitrary[Country]) {
            (mode, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(CountryOfRoutingPage(Index(0)), country)

              val result = new AddAnotherCountryViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems.length mustEqual 1
              result.title mustEqual "You have added 1 country to the transit route"
              result.heading mustEqual "You have added 1 country to the transit route"
              result.legend mustEqual "Do you want to add another country to the transit route?"
              result.maxLimitLabel mustEqual "You cannot add another country to the transit route. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(1)
          }
        }

        "when there are multiple countries" in {
          forAll(arbitrary[Mode], arbitrary[Country]) {
            (mode, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(CountryOfRoutingPage(Index(0)), country)
                .setValue(CountryOfRoutingPage(Index(1)), country)
                .setValue(CountryOfRoutingPage(Index(2)), country)
                .setValue(CountryOfRoutingPage(Index(3)), country)

              val result = new AddAnotherCountryViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems.length mustEqual 4
              result.title mustEqual s"You have added 4 countries to the transit route"
              result.heading mustEqual s"You have added 4 countries to the transit route"
              result.legend mustEqual "Do you want to add another country to the transit route?"
              result.maxLimitLabel mustEqual "You cannot add another country to the transit route. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(4)
          }
        }

        "when one has been removed" in {
          forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], Gen.alphaStr, arbitrary[Country]) {
            (mode, identification, identificationNumber, country) =>
              val userAnswers = emptyUserAnswers
                .setValue(CountryOfRoutingPage(Index(0)), country)
                .setRemoved(CountryOfRoutingSection(Index(1)))
                .setValue(CountryOfRoutingPage(Index(2)), country)
                .setValue(CountryOfRoutingPage(Index(3)), country)

              val result = new AddAnotherCountryViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems.length mustEqual 3
              result.title mustEqual s"You have added 3 countries to the transit route"
              result.heading mustEqual s"You have added 3 countries to the transit route"
              result.legend mustEqual "Do you want to add another country to the transit route?"
              result.maxLimitLabel mustEqual "You cannot add another country to the transit route. To add another, you need to remove one first."
              result.nextIndex mustEqual Index(4)
          }
        }

        "show remove links" in {
          forAll(arbitrary[Mode], arbitrary[Country], arbitrary[Country]) {
            (mode, country1, country2) =>
              val userAnswers = emptyUserAnswers
                .setValue(CountryOfRoutingPage(Index(0)), country1)
                .setValue(CountryOfRoutingPage(Index(1)), country2)

              val result = new AddAnotherCountryViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems mustEqual Seq(
                ListItem(
                  name = country1.toString,
                  changeUrl = None,
                  removeUrl = Some(routes.RemoveCountryYesNoController.onPageLoad(arrivalId, mode, Index(0)).url)
                ),
                ListItem(
                  name = country2.toString,
                  changeUrl = None,
                  removeUrl = Some(routes.RemoveCountryYesNoController.onPageLoad(arrivalId, mode, Index(1)).url)
                )
              )

              result.nextIndex mustEqual Index(2)
          }
        }
      }
    }
  }
}
