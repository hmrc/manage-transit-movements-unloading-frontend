/*
 * Copyright 2025 HM Revenue & Customs
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

package utils.answersHelpers.consignment

import base.AppWithDefaultMockFixtures
import models.CheckMode
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import pages.countriesOfRouting.CountryOfRoutingPage
import utils.answersHelpers.AnswersHelperSpecBase

class CountryOfRoutingAnswersHelperSpec extends AnswersHelperSpecBase {

  "CountryOfRoutingAnswersHelper" - {

    "country" - {
      val page = CountryOfRoutingPage(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new CountryOfRoutingAnswersHelper(emptyUserAnswers, index)
          helper.country mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new CountryOfRoutingAnswersHelper(answers, index)
              val result = helper.country.value

              result.key.value mustBe "Country 1"
              result.value.value mustBe value.toString
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.countriesOfRouting.routes.CountryController.onPageLoad(arrivalId, index, CheckMode).url
              action.visuallyHiddenText.value mustBe "country 1"
              action.id mustBe "change-country-1"
          }
        }
      }
    }
  }

}
