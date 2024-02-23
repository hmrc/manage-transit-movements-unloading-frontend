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

import models.reference.DocumentType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.documents._
import utils.answersHelpers.AnswersHelperSpecBase

class DocumentAnswersHelperSpec extends AnswersHelperSpecBase {

  "DocumentAnswersHelper" - {

    "documentType" - {
      val page = TypePage(documentIndex)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          helper.documentType mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[DocumentType]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.documentType.value

              result.key.value mustBe "Document type"
              result.value.value mustBe value.toString
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.href mustBe "#"
              action.visuallyHiddenText.value mustBe "document type for document 1"
              action.id mustBe "change-document-type-1"
          }
        }
      }
    }

    "referenceNumber" - {
      val page = DocumentReferenceNumberPage(documentIndex)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          helper.referenceNumber mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.referenceNumber.value

              result.key.value mustBe "Reference number"
              result.value.value mustBe value
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.href mustBe "#"
              action.visuallyHiddenText.value mustBe "reference number for document 1"
              action.id mustBe "change-document-reference-number-1"
          }
        }
      }
    }

    "additionalInformation" - {
      val page = AdditionalInformationPage(documentIndex)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          helper.additionalInformation mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.additionalInformation.value

              result.key.value mustBe "Additional information"
              result.value.value mustBe value
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.href mustBe "#"
              action.visuallyHiddenText.value mustBe "additional information for document 1"
              action.id mustBe "change-document-additional-information-1"
          }
        }
      }
    }
  }
}