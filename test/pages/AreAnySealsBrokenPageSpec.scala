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

class AreAnySealsBrokenPageSpec extends PageBehaviours {

  "AreAnySealsBrokenPage" - {

    beRetrievable[Boolean](AreAnySealsBrokenPage)

    beSettable[Boolean](AreAnySealsBrokenPage)

    beRemovable[Boolean](AreAnySealsBrokenPage)

    "cleanup" - {
      "must remove answer to AddTransitUnloadingPermissionDiscrepanciesYesNoPage when state of seals = 0 and using legacy" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(CanSealsBeReadPage, true)
          .setValue(AreAnySealsBrokenPage, false)
          .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

        val result = userAnswers.setValue(AreAnySealsBrokenPage, true)

        result.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) must not be defined
      }

      "must keep answer to AddTransitUnloadingPermissionDiscrepanciesYesNoPage when state of seals = 0 and switched from revised to legacy" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)
          .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)
          .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
          .setValue(CanSealsBeReadPage, true)
          .setValue(AreAnySealsBrokenPage, true)

        val result = userAnswers.setValue(AreAnySealsBrokenPage, false)

        result.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) must be(defined)
      }

      "must keep answer to AddTransitUnloadingPermissionDiscrepanciesYesNoPage when state of seals = 1" in {
        val userAnswers = emptyUserAnswers
          .setValue(CanSealsBeReadPage, true)
          .setValue(AreAnySealsBrokenPage, true)
          .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

        val result = userAnswers.setValue(AreAnySealsBrokenPage, false)

        result.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) must be(defined)
      }
    }
  }
}
