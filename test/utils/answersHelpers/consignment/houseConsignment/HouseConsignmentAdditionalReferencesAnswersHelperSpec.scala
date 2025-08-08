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

package utils.answersHelpers.consignment.houseConsignment

import controllers.houseConsignment.index.additionalReference.routes
import models.CheckMode
import models.reference.AdditionalReferenceType
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.additionalReference.{HouseConsignmentAdditionalReferenceNumberPage, HouseConsignmentAdditionalReferenceTypePage}
import utils.answersHelpers.AnswersHelperSpecBase

class HouseConsignmentAdditionalReferencesAnswersHelperSpec extends AnswersHelperSpecBase {

  "HouseConsignmentAdditionalReferencesAnswersHelper" - {

    "referenceType" - {
      val page = HouseConsignmentAdditionalReferenceTypePage(hcIndex, index)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAdditionalReferencesAnswersHelper(emptyUserAnswers, hcIndex, index)
          helper.referenceType must not be defined
        }
      }

      "must return Some(Row)" in {
        forAll(arbitrary[AdditionalReferenceType]) {
          value =>
            val answers = emptyUserAnswers.setValue(page, value)

            val helper = new HouseConsignmentAdditionalReferencesAnswersHelper(answers, hcIndex, index)
            val result = helper.referenceType.value

            result.key.value mustEqual "Type"
            result.value.value mustEqual value.toString
            val action = result.actions.value.items.head
            action.content.value mustEqual "Change"
            action.href mustEqual routes.AdditionalReferenceTypeController.onPageLoad(arrivalId, CheckMode, CheckMode, hcIndex, index).url
            action.visuallyHiddenText.value mustEqual "type for additional reference 1"
            action.id mustEqual "change-additional-reference-type-1"
        }
      }
    }

    "referenceNumber" - {
      val page = HouseConsignmentAdditionalReferenceNumberPage(hcIndex, index)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAdditionalReferencesAnswersHelper(emptyUserAnswers, hcIndex, index)
          helper.referenceNumber must not be defined
        }
      }

      "must return Some(Row)" in {
        forAll(nonEmptyString) {
          value =>
            val answers = emptyUserAnswers.setValue(page, value)

            val helper = new HouseConsignmentAdditionalReferencesAnswersHelper(answers, hcIndex, index)
            val result = helper.referenceNumber.value

            result.key.value mustEqual "Reference number"
            result.value.value mustEqual value
            val action = result.actions.value.items.head
            action.content.value mustEqual "Change"
            action.href mustEqual routes.AdditionalReferenceNumberController.onPageLoad(arrivalId, CheckMode, CheckMode, hcIndex, index).url
            action.visuallyHiddenText.value mustEqual "reference number for additional reference 1"
            action.id mustEqual "change-additional-reference-number-1"
        }
      }
    }
  }
}
