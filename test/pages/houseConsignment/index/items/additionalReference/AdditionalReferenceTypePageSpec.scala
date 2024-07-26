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

import models.reference.AdditionalReferenceType
import pages.behaviours.PageBehaviours
import org.scalacheck.Arbitrary.arbitrary

class AdditionalReferenceTypePageSpec extends PageBehaviours {

  "AdditionalReferencePage" - {

    beRetrievable[AdditionalReferenceType](AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))

    beSettable[AdditionalReferenceType](AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))

    beRemovable[AdditionalReferenceType](AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))

    "when value changes" - {
      "must clean up subsequent pages" in {
        forAll(arbitrary[AdditionalReferenceType]) {
          value =>
            forAll(arbitrary[AdditionalReferenceType].retryUntil(_ != value), nonEmptyString) {
              (differentValue, referenceNumber) =>
                val userAnswers = emptyUserAnswers
                  .setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), value)
                  .setValue(AddAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), true)
                  .setValue(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), referenceNumber)

                val result = userAnswers.setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), differentValue)

                result.get(AddAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) mustNot be(defined)
                result.get(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) mustNot be(defined)
            }
        }
      }
    }

    "when value has not changed" - {
      "must not clean up subsequent pages" in {
        forAll(arbitrary[AdditionalReferenceType], nonEmptyString) {
          (value, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), value)
              .setValue(AddAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), true)
              .setValue(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), referenceNumber)

            val result = userAnswers.setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), value)

            result.get(AddAdditionalReferenceNumberYesNoPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) must be(defined)
            result.get(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) must be(defined)
        }
      }
    }
  }
}
