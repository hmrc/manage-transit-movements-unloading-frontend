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

package pages.additionalReference

import org.scalacheck.Gen
import pages.behaviours.PageBehaviours

class AdditionalReferenceNumberYesNoPageSpec extends PageBehaviours {

  "AdditionalIdentificationNumberYesNoPage" - {

    beRetrievable[Boolean](AdditionalReferenceNumberYesNoPage(index))

    beSettable[Boolean](AdditionalReferenceNumberYesNoPage(index))

    beRemovable[Boolean](AdditionalReferenceNumberYesNoPage(index))

    "cleanup" - {
      "must remove additional reference number when no selected" in {
        forAll(Gen.alphaNumStr) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalReferenceNumberYesNoPage(index), true)
              .setValue(AdditionalReferenceNumberPage(index), value)

            val result = userAnswers.setValue(AdditionalReferenceNumberYesNoPage(index), false)

            result.get(AdditionalReferenceNumberPage(index)) must not be defined

        }
      }

      "must keep additional reference number when yes selected" in {
        forAll(Gen.alphaNumStr) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalReferenceNumberYesNoPage(index), true)
              .setValue(AdditionalReferenceNumberPage(index), value)

            val result = userAnswers.setValue(AdditionalReferenceNumberYesNoPage(index), true)

            result.get(AdditionalReferenceNumberPage(index)) mustBe defined
        }
      }
    }
  }
}
