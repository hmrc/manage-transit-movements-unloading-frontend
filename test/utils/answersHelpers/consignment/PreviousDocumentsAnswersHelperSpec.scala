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

import models.DocType.Previous
import models.reference._
import org.scalacheck.Gen
import pages.houseConsignment.previousDocument.{AdditionalInformationPage, DocumentReferenceNumberPage, TypePage}
import utils.answersHelpers.AnswersHelperSpecBase
import utils.answersHelpers.consignment.houseConsignment.PreviousDocumentsAnswersHelper

class PreviousDocumentsAnswersHelperSpec extends AnswersHelperSpecBase {

  "PreviousDocumentsAnswersHelper" - {

    "previousDocumentType" - {
      val page = TypePage(hcIndex, documentIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new PreviousDocumentsAnswersHelper(emptyUserAnswers, hcIndex, documentIndex)
          helper.previousDocumentType mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (code, description) =>
              val docType = DocumentType(Previous, code, description)
              val answers = emptyUserAnswers.setValue(page, docType)

              val helper = new PreviousDocumentsAnswersHelper(answers, hcIndex, documentIndex)
              val result = helper.previousDocumentType.value

              result.key.value mustBe "Type"
              result.value.value mustBe docType.toString
          }
        }
      }
    }

    "previousDocumentReferenceNumber" - {
      val page = DocumentReferenceNumberPage(hcIndex, documentIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new PreviousDocumentsAnswersHelper(emptyUserAnswers, hcIndex, documentIndex)
          helper.previousDocumentReferenceNumber mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new PreviousDocumentsAnswersHelper(answers, hcIndex, documentIndex)
              val result = helper.previousDocumentReferenceNumber.value

              result.key.value mustBe "Reference Number"
              result.value.value mustBe value
          }
        }
      }
    }

    "previousDocumentInformation" - {
      val page = AdditionalInformationPage(hcIndex, documentIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new PreviousDocumentsAnswersHelper(emptyUserAnswers, hcIndex, documentIndex)
          helper.previousDocumentInformation mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new PreviousDocumentsAnswersHelper(answers, hcIndex, documentIndex)
              val result = helper.previousDocumentInformation.value

              result.key.value mustBe "Additional Information"
              result.value.value mustBe value
          }
        }
      }
    }

  }
}
