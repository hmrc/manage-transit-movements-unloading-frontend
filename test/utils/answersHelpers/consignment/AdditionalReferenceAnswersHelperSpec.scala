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

package utils.answersHelpers.consignment

import base.AppWithDefaultMockFixtures
import models.CheckMode
import models.reference.AdditionalReferenceType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.additionalReference.*
import utils.answersHelpers.AnswersHelperSpecBase

class AdditionalReferenceAnswersHelperSpec extends AnswersHelperSpecBase {

  "AdditionalReferenceAnswersHelper" - {

    "code" - {
      val page = AdditionalReferenceTypePage(index)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new AdditionalReferenceAnswersHelper(emptyUserAnswers, index)
          helper.code must not be defined
        }
      }

      "must return Some(Row)" in {
        forAll(arbitrary[AdditionalReferenceType]) {
          value =>
            val answers = emptyUserAnswers.setValue(page, value)

            val helper = new AdditionalReferenceAnswersHelper(answers, index)
            val result = helper.code.value

            result.key.value mustEqual "Type"
            result.value.value mustEqual value.toString
            val action = result.actions.value.items.head
            action.content.value mustEqual "Change"
            action.href mustEqual controllers.additionalReference.index.routes.AdditionalReferenceTypeController.onPageLoad(arrivalId, CheckMode, index).url
            action.visuallyHiddenText.value mustEqual "type for additional reference 1"
            action.id mustEqual "change-additional-reference-type-1"
        }
      }
    }

    "referenceNumber" - {
      val page = AdditionalReferenceNumberPage(index)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new AdditionalReferenceAnswersHelper(emptyUserAnswers, index)
          helper.referenceNumber must not be defined
        }
      }

      "must return Some(Row)" in {
        forAll(Gen.alphaNumStr) {
          value =>
            val answers = emptyUserAnswers.setValue(page, value)

            val helper = new AdditionalReferenceAnswersHelper(answers, index)
            val result = helper.referenceNumber.value

            result.key.value mustEqual "Reference number"
            result.value.value mustEqual value
            val action = result.actions.value.items.head
            action.content.value mustEqual "Change"
            action.href mustEqual controllers.additionalReference.index.routes.AdditionalReferenceNumberController.onPageLoad(arrivalId, index, CheckMode).url
            action.visuallyHiddenText.value mustEqual "reference number for additional reference 1"
            action.id mustEqual "change-additional-reference-number-1"
        }
      }
    }
  }
}
