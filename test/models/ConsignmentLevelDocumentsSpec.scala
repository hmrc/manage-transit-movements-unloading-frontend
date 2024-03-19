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

package models

import base.SpecBase
import generators.Generators
import models.reference.DocumentType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.documents.TypePage

class ConsignmentLevelDocumentsSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Consignment Level Documents" - {

    "must return counts of each document type at consignment level" - {

      "when there is a supporting document" - {

        "when index not provided" in {
          forAll(arbitrary[DocumentType](arbitrarySupportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(index), document)

              val result = ConsignmentLevelDocuments.apply(userAnswers)
              result.supporting mustBe 1
              result.transport mustBe 0
          }
        }

        "when filtering out current index (to allow an amend)" in {
          forAll(arbitrary[DocumentType](arbitrarySupportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(index), document)

              val result = ConsignmentLevelDocuments.apply(userAnswers, Some(index))
              result.supporting mustBe 0
              result.transport mustBe 0
          }
        }
      }

      "when there is a transport document" - {

        "when index not provided" in {
          forAll(arbitrary[DocumentType](arbitraryTransportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(index), document)

              val result = ConsignmentLevelDocuments.apply(userAnswers)
              result.supporting mustBe 0
              result.transport mustBe 1
          }
        }

        "when filtering out current index (to allow an amend)" in {
          forAll(arbitrary[DocumentType](arbitraryTransportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(index), document)

              val result = ConsignmentLevelDocuments.apply(userAnswers, Some(index))
              result.supporting mustBe 0
              result.transport mustBe 0
          }
        }
      }
    }

    "availableDocuments must filter the document that can be added" in {
      val supportDoc   = arbitrary[DocumentType](arbitrarySupportDocument).sample.value
      val transportDoc = arbitrary[DocumentType](arbitraryTransportDocument).sample.value
      val documents    = Seq(transportDoc, supportDoc)

      val consignmentLevelDocuments = ConsignmentLevelDocuments(frontendAppConfig.maxSupportingDocuments - 1, frontendAppConfig.maxTransportDocuments)

      consignmentLevelDocuments.availableDocuments(documents) mustBe Seq(supportDoc)
    }
  }
}
