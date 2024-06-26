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

class DoYouHaveAnythingElseToReportYesNoPageSpec extends PageBehaviours {

  "DoYouHaveAnythingElseToReportYesNoPage" - {

    beRetrievable[Boolean](DoYouHaveAnythingElseToReportYesNoPage)

    beSettable[Boolean](DoYouHaveAnythingElseToReportYesNoPage)

    beRemovable[Boolean](DoYouHaveAnythingElseToReportYesNoPage)

    "cleanup" - {
      "must remove other things reported when no selected" in {
        val userAnswers = emptyUserAnswers
          .setValue(DoYouHaveAnythingElseToReportYesNoPage, true)
          .setValue(OtherThingsToReportPage, "Other things reported")

        val result = userAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, false)

        result.get(OtherThingsToReportPage) must not be defined
      }

      "must keep other things reported when yes selected" in {
        val userAnswers = emptyUserAnswers
          .setValue(DoYouHaveAnythingElseToReportYesNoPage, true)
          .setValue(OtherThingsToReportPage, "comments")

        val result = userAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, true)

        result.get(OtherThingsToReportPage) must be(defined)
      }
    }
  }
}
