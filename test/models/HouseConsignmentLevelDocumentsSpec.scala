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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.DocumentType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.document.TypePage

class HouseConsignmentLevelDocumentsSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "House Consignment Level Documents" - {

    "must return counts of each document type at house consignment level" - {

      "when there is a supporting document" - {

        "when document index not provided" in {
          forAll(arbitrary[DocumentType](arbitrarySupportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(houseConsignmentIndex, itemIndex, documentIndex), document)

              val result = HouseConsignmentLevelDocuments.apply(userAnswers, houseConsignmentIndex, itemIndex, None)
              result.supporting mustEqual 1
              result.transport mustEqual 0
          }
        }

        "when filtering out current document index (to allow an amend)" in {
          forAll(arbitrary[DocumentType](arbitrarySupportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(houseConsignmentIndex, itemIndex, documentIndex), document)

              val result = HouseConsignmentLevelDocuments.apply(userAnswers, houseConsignmentIndex, itemIndex, Some(index))
              result.supporting mustEqual 0
              result.transport mustEqual 0
          }
        }

        "when not at item level" in {
          import pages.houseConsignment.index.documents.TypePage

          forAll(arbitrary[DocumentType](arbitrarySupportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(houseConsignmentIndex, documentIndex), document)

              val result = HouseConsignmentLevelDocuments.apply(userAnswers, houseConsignmentIndex, documentIndex)
              result.supporting mustEqual 0
              result.transport mustEqual 0
          }
        }
      }

      "when there is a transport document" - {

        "when document index not provided" in {
          forAll(arbitrary[DocumentType](arbitraryTransportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(houseConsignmentIndex, itemIndex, documentIndex), document)

              val result = HouseConsignmentLevelDocuments.apply(userAnswers, houseConsignmentIndex, itemIndex, None)
              result.supporting mustEqual 0
              result.transport mustEqual 1
          }
        }

        "when filtering out current document index (to allow an amend)" in {
          forAll(arbitrary[DocumentType](arbitraryTransportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(houseConsignmentIndex, itemIndex, documentIndex), document)

              val result = HouseConsignmentLevelDocuments.apply(userAnswers, houseConsignmentIndex, itemIndex, Some(index))
              result.supporting mustEqual 0
              result.transport mustEqual 0
          }
        }

        "when not at item level" in {
          import pages.houseConsignment.index.documents.TypePage

          forAll(arbitrary[DocumentType](arbitraryTransportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(houseConsignmentIndex, documentIndex), document)

              val result = HouseConsignmentLevelDocuments.apply(userAnswers, houseConsignmentIndex, documentIndex)
              result.supporting mustEqual 0
              result.transport mustEqual 0
          }
        }
      }
    }

    "availableDocuments must filter the document that can be added" in {
      val supportDoc   = arbitrary[DocumentType](arbitrarySupportDocument).sample.value
      val transportDoc = arbitrary[DocumentType](arbitraryTransportDocument).sample.value
      val documents    = Seq(transportDoc, supportDoc)

      val houseConsignmentLevelDocuments =
        HouseConsignmentLevelDocuments(frontendAppConfig.maxSupportingDocumentsHouseConsignment - 1, frontendAppConfig.maxTransportDocumentsHouseConsignment)

      houseConsignmentLevelDocuments.availableDocuments(documents) mustEqual Seq(supportDoc)
    }
  }
}
