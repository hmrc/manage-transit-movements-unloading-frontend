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

package pages.houseConsignment.index.additionalReference

import pages.behaviours.PageBehaviours

class AddHouseConsignmentAdditionalReferenceNumberYesNoPageSpec extends PageBehaviours {

  "AddHouseConsignmentAdditionalIdentificationNumberYesNoPage" - {

    beRetrievable[Boolean](AddHouseConsignmentAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, additionalReferenceIndex))

    beSettable[Boolean](AddHouseConsignmentAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, additionalReferenceIndex))

    beRemovable[Boolean](AddHouseConsignmentAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, additionalReferenceIndex))

    "cleanup" - {
      "must remove additional reference number when no selected" in {
        forAll(nonEmptyString) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, additionalReferenceIndex), true)
              .setValue(HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, additionalReferenceIndex), value)

            val result = userAnswers.setValue(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, additionalReferenceIndex), false)

            result.get(HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, additionalReferenceIndex)) must not be defined

        }
      }

      "must keep additional reference number when yes selected" in {
        forAll(nonEmptyString) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, additionalReferenceIndex), true)
              .setValue(HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, additionalReferenceIndex), value)

            val result = userAnswers.setValue(AddHouseConsignmentAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, additionalReferenceIndex), true)

            result.get(HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, additionalReferenceIndex)) mustBe defined
        }
      }
    }
  }
}
