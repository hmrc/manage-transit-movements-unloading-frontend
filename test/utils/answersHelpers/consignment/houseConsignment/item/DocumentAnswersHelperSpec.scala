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

import controllers.houseConsignment.index.items.document.routes
import models.CheckMode
import models.reference.DocumentType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.houseConsignment.index.items.document._
import utils.answersHelpers.AnswersHelperSpecBase

class DocumentAnswersHelperSpec extends AnswersHelperSpecBase {

  "DocumentAnswersHelper" - {

    "documentType" - {
      val page = TypePage(hcIndex, itemIndex, documentIndex)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, hcIndex, itemIndex, documentIndex)
          helper.documentType() must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[DocumentType]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DocumentAnswersHelper(answers, hcIndex, itemIndex, documentIndex)
              val result = helper.documentType().value

              result.key.value mustEqual "Document type"
              result.value.value mustEqual value.toString
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.href mustEqual routes.TypeController.onPageLoad(arrivalId, CheckMode, CheckMode, CheckMode, hcIndex, itemIndex, documentIndex).url
              action.visuallyHiddenText.value mustEqual "document type for document 1 in item 1"
              action.id mustEqual "change-document-type-1-1"
          }
        }
      }

      "must return Some(Row) without links" - {
        s"when $page read only" in {
          forAll(arbitrary[DocumentType]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DocumentAnswersHelper(answers, hcIndex, itemIndex, documentIndex)
              val result = helper.documentType(true).value

              result.key.value mustEqual "Document type"
              result.value.value mustEqual value.toString
              result.actions must not be defined
          }
        }
      }
    }

    "referenceNumber" - {
      val page = DocumentReferenceNumberPage(hcIndex, itemIndex, documentIndex)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, hcIndex, itemIndex, documentIndex)
          helper.referenceNumber() must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(nonEmptyString) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DocumentAnswersHelper(answers, hcIndex, itemIndex, documentIndex)
              val result = helper.referenceNumber().value

              result.key.value mustEqual "Reference number"
              result.value.value mustEqual value
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.href mustEqual routes.DocumentReferenceNumberController
                .onPageLoad(arrivalId, CheckMode, CheckMode, CheckMode, hcIndex, itemIndex, documentIndex)
                .url
              action.visuallyHiddenText.value mustEqual "reference number for document 1 in item 1"
              action.id mustEqual "change-document-reference-number-1-1"
          }
        }
      }

      "must return Some(Row) without links" - {
        s"when $page read only" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DocumentAnswersHelper(answers, hcIndex, itemIndex, documentIndex)
              val result = helper.referenceNumber(true).value

              result.key.value mustEqual "Reference number"
              result.value.value mustEqual value
              result.actions must not be defined
          }
        }
      }
    }

    "additionalInformation" - {
      val page = AdditionalInformationPage(hcIndex, itemIndex, documentIndex)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, hcIndex, itemIndex, documentIndex)
          helper.additionalInformation() must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DocumentAnswersHelper(answers, hcIndex, itemIndex, documentIndex)
              val result = helper.additionalInformation().value

              result.key.value mustEqual "Additional information"
              result.value.value mustEqual value
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.href mustEqual routes.AdditionalInformationController
                .onPageLoad(arrivalId, CheckMode, CheckMode, CheckMode, hcIndex, itemIndex, documentIndex)
                .url
              action.visuallyHiddenText.value mustEqual "additional information for document 1 in item 1"
              action.id mustEqual "change-document-additional-information-1-1"
          }
        }
      }

      "must return Some(Row) without links" - {
        s"when $page read only" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DocumentAnswersHelper(answers, hcIndex, itemIndex, documentIndex)
              val result = helper.additionalInformation(true).value

              result.key.value mustEqual "Additional information"
              result.value.value mustEqual value
              result.actions must not be defined
          }
        }
      }
    }
  }
}
