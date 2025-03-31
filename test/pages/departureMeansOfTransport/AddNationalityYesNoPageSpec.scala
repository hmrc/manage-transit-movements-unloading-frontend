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

package pages.departureMeansOfTransport

import models.reference.Country
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class AddNationalityYesNoPageSpec extends PageBehaviours {

  "AddNationalityYesNoPage" - {

    beRetrievable[Boolean](AddNationalityYesNoPage(index))

    beSettable[Boolean](AddNationalityYesNoPage(index))

    beRemovable[Boolean](AddNationalityYesNoPage(index))

    "cleanup" - {
      "must remove nationality page when no selected" in {
        forAll(Arbitrary.arbitrary[Country]) {
          country =>
            val userAnswers = emptyUserAnswers
              .setValue(AddNationalityYesNoPage(index), true)
              .setValue(CountryPage(index), country)

            val result = userAnswers.setValue(AddNationalityYesNoPage(index), false)

            result.get(CountryPage(index)) must not be defined
        }
      }

      "must keep identification when yes selected" in {
        forAll(Arbitrary.arbitrary[Country]) {
          country =>
            val userAnswers = emptyUserAnswers
              .setValue(AddNationalityYesNoPage(index), true)
              .setValue(CountryPage(index), country)

            val result = userAnswers.setValue(AddNationalityYesNoPage(index), true)

            result.get(CountryPage(index)) mustBe defined
        }
      }
    }
  }
}
