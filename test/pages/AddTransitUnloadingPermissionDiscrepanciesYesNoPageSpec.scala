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

class AddTransitUnloadingPermissionDiscrepanciesYesNoPageSpec extends PageBehaviours {

  "AddTransitUnloadingPermissionDiscrepanciesYesNoPage" - {

    beRetrievable[Boolean](AddTransitUnloadingPermissionDiscrepanciesYesNoPage)

    beSettable[Boolean](AddTransitUnloadingPermissionDiscrepanciesYesNoPage)

    beRemovable[Boolean](AddTransitUnloadingPermissionDiscrepanciesYesNoPage)

    "cleanup" - {
      "when no selected" - {
        "must remove answers for AddCommentsYesNoPage and UnloadingCommentsPage" in {
          forAll(nonEmptyString) {
            comments =>
              val userAnswers = emptyUserAnswers
                .setValue(AddCommentsYesNoPage, true)
                .setValue(UnloadingCommentsPage, comments)

              val result = userAnswers.setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

              result.get(AddCommentsYesNoPage) must not be defined
              result.get(UnloadingCommentsPage) must not be defined
          }
        }
      }
      "when yes selected" - {
        "must keep answers for AddCommentsYesNoPage and UnloadingCommentsPage" in {
          forAll(nonEmptyString) {
            comments =>
              val userAnswers = emptyUserAnswers
                .setValue(AddCommentsYesNoPage, true)
                .setValue(UnloadingCommentsPage, comments)

              val result = userAnswers.setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

              result.get(AddCommentsYesNoPage) mustBe defined
              result.get(UnloadingCommentsPage) mustBe defined
          }
        }
      }
    }
  }
}
