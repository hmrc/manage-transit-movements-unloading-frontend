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

import models.reference.AdditionalReferenceType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.houseConsignment.index.items.additionalReference._
import utils.answersHelpers.AnswersHelperSpecBase

class AdditionalReferencesAnswersHelperSpec extends AnswersHelperSpecBase {

  "AdditionalReferencesAnswersHelper" - {

    "code" - {
      val page = AdditionalReferencePage(hcIndex, itemIndex, index)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new AdditionalReferencesAnswerHelper(emptyUserAnswers, hcIndex, itemIndex, index)
          helper.code mustBe None
        }
      }

      "must return Some(Row)" in {
        forAll(arbitrary[AdditionalReferenceType]) {
          value =>
            val answers = emptyUserAnswers.setValue(page, value)

            val helper = new AdditionalReferencesAnswerHelper(answers, hcIndex, itemIndex, index)
            val result = helper.code.value

            result.key.value mustBe "Type"
            result.value.value mustBe value.toString
            val action = result.actions.value.items.head
            action.content.value mustBe "Change"
            action.href mustBe "#"
            action.visuallyHiddenText.value mustBe "type for additional reference 1 in item 1"
            action.id mustBe "change-additional-reference-type-1-1"
        }
      }
    }

    "referenceNumber" - {
      val page = AdditionalReferenceNumberPage(hcIndex, itemIndex, index)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new AdditionalReferencesAnswerHelper(emptyUserAnswers, hcIndex, itemIndex, index)
          helper.referenceNumber mustBe None
        }
      }

      "must return Some(Row)" in {
        forAll(Gen.alphaNumStr) {
          value =>
            val answers = emptyUserAnswers.setValue(page, value)

            val helper = new AdditionalReferencesAnswerHelper(answers, hcIndex, itemIndex, index)
            val result = helper.referenceNumber.value

            result.key.value mustBe "Reference number"
            result.value.value mustBe value
            val action = result.actions.value.items.head
            action.content.value mustBe "Change"
            action.href mustBe "#"
            action.visuallyHiddenText.value mustBe "reference number for additional reference 1 in item 1"
            action.id mustBe "change-additional-reference-number-1-1"
        }
      }
    }
  }
}
