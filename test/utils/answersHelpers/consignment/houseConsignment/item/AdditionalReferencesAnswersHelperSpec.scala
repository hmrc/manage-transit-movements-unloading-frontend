/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.answersHelpers.consignment.houseConsignment.item

import controllers.houseConsignment.index.items.additionalReference.routes
import models.CheckMode
import models.reference.AdditionalReferenceType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.houseConsignment.index.items.additionalReference._
import utils.answersHelpers.AnswersHelperSpecBase

class AdditionalReferencesAnswersHelperSpec extends AnswersHelperSpecBase {

  "AdditionalReferencesAnswersHelper" - {

    "code" - {
      val page = AdditionalReferenceTypePage(hcIndex, itemIndex, index)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new AdditionalReferencesAnswerHelper(emptyUserAnswers, hcIndex, itemIndex, index)
          helper.code must not be defined
        }
      }

      "must return Some(Row)" in {
        forAll(arbitrary[AdditionalReferenceType]) {
          value =>
            val answers = emptyUserAnswers.setValue(page, value)

            val helper = new AdditionalReferencesAnswerHelper(answers, hcIndex, itemIndex, index)
            val result = helper.code.value

            result.key.value mustEqual "Type"
            result.value.value mustEqual value.toString
            val action = result.actions.value.items.head
            action.content.value mustEqual "Change"
            action.href mustEqual routes.AdditionalReferenceTypeController.onPageLoad(arrivalId, CheckMode, CheckMode, CheckMode, hcIndex, itemIndex, index).url
            action.visuallyHiddenText.value mustEqual "type for additional reference 1 in item 1"
            action.id mustEqual "change-additional-reference-type-1-1"
        }
      }
    }

    "referenceNumber" - {
      val page = AdditionalReferenceNumberPage(hcIndex, itemIndex, index)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new AdditionalReferencesAnswerHelper(emptyUserAnswers, hcIndex, itemIndex, index)
          helper.referenceNumber must not be defined
        }
      }

      "must return Some(Row)" in {
        forAll(Gen.alphaNumStr) {
          value =>
            val answers = emptyUserAnswers.setValue(page, value)

            val helper = new AdditionalReferencesAnswerHelper(answers, hcIndex, itemIndex, index)
            val result = helper.referenceNumber.value

            result.key.value mustEqual "Reference number"
            result.value.value mustEqual value
            val action = result.actions.value.items.head
            action.content.value mustEqual "Change"
            action.href mustEqual routes.AdditionalReferenceNumberController
              .onPageLoad(arrivalId, CheckMode, CheckMode, CheckMode, hcIndex, itemIndex, index)
              .url
            action.visuallyHiddenText.value mustEqual "reference number for additional reference 1 in item 1"
            action.id mustEqual "change-additional-reference-number-1-1"
        }
      }
    }
  }
}
