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

package pages.houseConsignment.index.documents

import org.scalacheck.Gen
import pages.behaviours.PageBehaviours

class AddAdditionalInformationYesNoPageSpec extends PageBehaviours {

  "AddAdditionalInformationYesNoPageSpec" - {

    beRetrievable[Boolean](AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex))

    beSettable[Boolean](AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex))

    beRemovable[Boolean](AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex))
  }

  "cleanup" - {
    "must remove additional information when no selected" in {
      forAll(Gen.alphaNumStr) {
        value =>
          val userAnswers = emptyUserAnswers
            .setValue(AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex), true)
            .setValue(AdditionalInformationPage(houseConsignmentIndex, documentIndex), value)

          val result = userAnswers.setValue(AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex), false)

          result.get(AdditionalInformationPage(houseConsignmentIndex, documentIndex)) must not be defined
      }
    }

    "must keep additional information when yes selected" in {
      forAll(Gen.alphaNumStr) {
        value =>
          val userAnswers = emptyUserAnswers
            .setValue(AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex), true)
            .setValue(AdditionalInformationPage(houseConsignmentIndex, documentIndex), value)

          val result = userAnswers.setValue(AddAdditionalInformationYesNoPage(houseConsignmentIndex, documentIndex), true)

          result.get(AdditionalInformationPage(houseConsignmentIndex, documentIndex)) mustBe defined
      }
    }
  }
}
