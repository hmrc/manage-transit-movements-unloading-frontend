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

package pages.documents

import org.scalacheck.Gen
import pages.behaviours.PageBehaviours

class AddAdditionalInformationYesNoPageSpec extends PageBehaviours {

  "AddAdditionalInformationYesNoPageSpec" - {

    beRetrievable[Boolean](AddAdditionalInformationYesNoPage(documentIndex))

    beSettable[Boolean](AddAdditionalInformationYesNoPage(documentIndex))

    beRemovable[Boolean](AddAdditionalInformationYesNoPage(documentIndex))
  }

  "cleanup" - {
    "must remove additional information when no selected" in {
      forAll(Gen.alphaNumStr) {
        value =>
          val userAnswers = emptyUserAnswers
            .setValue(AddAdditionalInformationYesNoPage(index), true)
            .setValue(AdditionalInformationPage(index), value)

          val result = userAnswers.setValue(AddAdditionalInformationYesNoPage(index), false)

          result.get(AdditionalInformationPage(index)) must not be defined
      }
    }

    "must keep additional information when yes selected" in {
      forAll(Gen.alphaNumStr) {
        value =>
          val userAnswers = emptyUserAnswers
            .setValue(AddAdditionalInformationYesNoPage(index), true)
            .setValue(AdditionalInformationPage(index), value)

          val result = userAnswers.setValue(AddAdditionalInformationYesNoPage(index), true)

          result.get(AdditionalInformationPage(index)) mustBe defined
      }
    }
  }
}
