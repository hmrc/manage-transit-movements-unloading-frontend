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

class AdditionalReferenceTypePageSpec extends PageBehaviours {

  "AdditionalReferencePage" - {

    beRetrievable[AdditionalReferenceType](AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))

    beSettable[AdditionalReferenceType](AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))

    beRemovable[AdditionalReferenceType](AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))
  }

  "AdditionalReferenceInCL234Page" - {
    "cleanup" - {
      "must cleanup" - {
        "when true and additional reference number is 0" in {
          val userAnswers = emptyUserAnswers
            .setValue(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), "0")

          val result = userAnswers.setValue(AdditionalReferenceInCL234Page(houseConsignmentIndex, itemIndex, additionalReferenceIndex), true)

          result.get(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) must not be defined
        }
      }

      "must not cleanup" - {
        "when true and additional reference number is not 0" in {
          forAll(nonEmptyString.retryUntil(_ != "0")) {
            referenceNumber =>
              val userAnswers = emptyUserAnswers
                .setValue(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), referenceNumber)

              val result = userAnswers.setValue(AdditionalReferenceInCL234Page(houseConsignmentIndex, itemIndex, additionalReferenceIndex), true)

              result.get(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) mustBe defined
          }
        }

        "when false and additional reference number is not 0" in {
          forAll(nonEmptyString.retryUntil(_ != "0")) {
            referenceNumber =>
              val userAnswers = emptyUserAnswers
                .setValue(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), referenceNumber)

              val result = userAnswers.setValue(AdditionalReferenceInCL234Page(houseConsignmentIndex, itemIndex, additionalReferenceIndex), false)

              result.get(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) mustBe defined
          }
        }
      }
    }
  }
}
