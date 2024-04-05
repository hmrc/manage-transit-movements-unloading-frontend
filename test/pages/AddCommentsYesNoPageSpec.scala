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

class AddCommentsYesNoPageSpec extends PageBehaviours {

  "AddCommentsYesNoPage" - {

    beRetrievable[Boolean](AddCommentsYesNoPage)

    beSettable[Boolean](AddCommentsYesNoPage)

    beRemovable[Boolean](AddCommentsYesNoPage)

    "cleanup" - {
      "must remove unloading comments when no selected" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddCommentsYesNoPage, true)
          .setValue(UnloadingCommentsPage, "comments")

        val result = userAnswers.setValue(AddCommentsYesNoPage, false)

        result.get(UnloadingCommentsPage) must not be defined
      }

      "must keep unloading comments when yes selected" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddCommentsYesNoPage, true)
          .setValue(UnloadingCommentsPage, "comments")

        val result = userAnswers.setValue(AddCommentsYesNoPage, true)

        result.get(UnloadingCommentsPage) must be(defined)
      }
    }
  }
}
