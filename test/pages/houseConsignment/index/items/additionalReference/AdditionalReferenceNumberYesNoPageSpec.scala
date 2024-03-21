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

package pages.houseConsignment.index.items.additionalReference

import pages.behaviours.PageBehaviours

class AdditionalReferenceNumberYesNoPageSpec extends PageBehaviours {

  "AdditionalIdentificationNumberYesNoPage" - {

    beRetrievable[Boolean](AdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))

    beSettable[Boolean](AdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))

    beRemovable[Boolean](AdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))

    "cleanup" - {
      "must remove additional reference number when no selected" in {
        forAll(nonEmptyString) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), true)
              .setValue(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), value)

            val result = userAnswers.setValue(AdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), false)

            result.get(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) must not be defined

        }
      }

      "must keep additional reference number when yes selected" in {
        forAll(nonEmptyString) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), true)
              .setValue(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), value)

            val result = userAnswers.setValue(AdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), true)

            result.get(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) mustBe defined
        }
      }
    }
  }
}
