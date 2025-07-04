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

package pages.houseConsignment.index

import pages.behaviours.PageBehaviours

class UniqueConsignmentReferenceYesNoPageSpec extends PageBehaviours {

  "AddUniqueConsignmentReferenceYesNoPage" - {

    beRetrievable[Boolean](UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex))

    beSettable[Boolean](UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex))

    beRemovable[Boolean](UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex))

    "cleanup" - {
      "must remove UCR when no selected" in {
        val userAnswers = emptyUserAnswers
          .setValue(UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex), true)
          .setValue(UniqueConsignmentReferencePage(houseConsignmentIndex), "foo")

        val result = userAnswers.setValue(UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex), false)

        result.get(UniqueConsignmentReferencePage(houseConsignmentIndex)) must not be defined
      }

      "must keep UCR when yes selected" in {
        val userAnswers = emptyUserAnswers
          .setValue(UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex), true)
          .setValue(UniqueConsignmentReferencePage(houseConsignmentIndex), "foo")

        val result = userAnswers.setValue(UniqueConsignmentReferenceYesNoPage(houseConsignmentIndex), true)

        result.get(UniqueConsignmentReferencePage(houseConsignmentIndex)) mustBe defined
      }
    }
  }
}
