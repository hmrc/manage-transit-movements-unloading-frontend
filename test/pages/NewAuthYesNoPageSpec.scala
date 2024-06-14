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

package pages

import pages.behaviours.PageBehaviours

class NewAuthYesNoPageSpec extends PageBehaviours {

  "NewAuthYesNoPage" - {

    beRetrievable[Boolean](NewAuthYesNoPage)

    beSettable[Boolean](NewAuthYesNoPage)

    beRemovable[Boolean](NewAuthYesNoPage)

    "cleanup" - {
      "must remove other things to report page when yes selected" in {
        forAll(nonEmptyString) {
          otherThingsToReport =>
            val userAnswers = emptyUserAnswers
              .setValue(OtherThingsToReportPage, otherThingsToReport)

            val result = userAnswers.setValue(NewAuthYesNoPage, true)

            result.get(OtherThingsToReportPage) must not be defined
        }
      }

      "must keep other things to report page when no selected" in {
        forAll(nonEmptyString) {
          otherThingsToReport =>
            val userAnswers = emptyUserAnswers
              .setValue(OtherThingsToReportPage, otherThingsToReport)

            val result = userAnswers.setValue(NewAuthYesNoPage, false)

            result.get(OtherThingsToReportPage) mustBe defined
        }
      }
    }
  }
}
