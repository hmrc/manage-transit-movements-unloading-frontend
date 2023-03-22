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

class AddUnloadingCommentsYesNoPageSpec extends PageBehaviours {

  "AddUnloadingCommentsYesNoPage" - {

    beRetrievable[Boolean](AddUnloadingCommentsYesNoPage)

    beSettable[Boolean](AddUnloadingCommentsYesNoPage)

    beRemovable[Boolean](AddUnloadingCommentsYesNoPage)

    "cleanup" - {
      "must remove contact details when no selected" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddUnloadingCommentsYesNoPage, true)
          .setValue(UnloadingCommentsPage, "i am a comment")

        val result = userAnswers.setValue(AddUnloadingCommentsYesNoPage, false)

        result.get(UnloadingCommentsPage) must not be defined
      }

      "must keep contact details when yes selected" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddUnloadingCommentsYesNoPage, true)
          .setValue(UnloadingCommentsPage, "i am a comment")

        val result = userAnswers.setValue(AddUnloadingCommentsYesNoPage, true)

        result.get(UnloadingCommentsPage) must be(defined)
      }
    }
  }
}
