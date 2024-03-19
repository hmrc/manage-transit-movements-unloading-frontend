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

package viewModels.documents

import base.SpecBase
import generators.Generators
import models.DocType.{Previous, Support, Transport}
import models.reference.DocumentType
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.documents.{DocumentReferenceNumberPage, TypePage}
import pages.sections.documents.DocumentSection
import viewModels.ListItem
import viewModels.documents.AddAnotherDocumentViewModel.AddAnotherDocumentViewModelProvider

class AddAnotherDocumentViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AddAnotherDocumentViewModelSpec" - {
    "list items" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val result = new AddAnotherDocumentViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems mustBe Nil
              result.title mustBe "You have added 0 document to all items"
              result.heading mustBe "You have added 0 document to all items"
              result.legend mustBe "Do you want to add a document to all items?"
              result.maxLimitLabel mustBe "You cannot add any more documents to all items. You can still add and attach documents to individual items from their house consignment cross-check screens."
              result.nextIndex mustBe Index(0)
          }
        }
      }

      "must get list items" - {

        "when there is one document" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(Index(0)), DocumentType(Support, "code1", "description1"))
                .setValue(DocumentReferenceNumberPage(Index(0)), "ref1")

              val result = new AddAnotherDocumentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems.length mustBe 1
              result.listItems.head.name mustBe "Support - ref1"
              result.title mustBe "You have added 1 document to all items"
              result.heading mustBe "You have added 1 document to all items"
              result.legend mustBe "Do you want to add another document to all items?"
              result.maxLimitLabel mustBe "You cannot add any more documents to all items. You can still add and attach documents to individual items from their house consignment cross-check screens."
              result.nextIndex mustBe Index(1)
          }
        }

        "when transport documents reached max limit" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val maxDocLimit  = frontendAppConfig.maxTransportDocuments
              val transportDoc = arbitrary[DocumentType](arbitraryTransportDocument).sample.value
              val maxTransportDocs = (0 until maxDocLimit).foldLeft(emptyUserAnswers) {
                (answers, index) =>
                  answers.setValue(TypePage(Index(index)), transportDoc)
              }
              val userAnswers = maxTransportDocs

              val result = new AddAnotherDocumentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.maxLimitLabelForType mustBe "You cannot add any more transport documents to all items. To add another, you need to remove one first. You can, however, still add a supporting document to your items."
              result.allowMore mustBe true
          }
        }

        "when supporting documents reached max limit" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val maxDocLimit = frontendAppConfig.maxSupportingDocuments
              val supportDoc  = arbitrary[DocumentType](arbitrarySupportDocument).sample.value
              val maxSupportDocs = (0 until maxDocLimit).foldLeft(emptyUserAnswers) {
                (answers, index) =>
                  answers.setValue(TypePage(Index(index)), supportDoc)
              }
              val userAnswers = maxSupportDocs

              val result = new AddAnotherDocumentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.maxLimitLabelForType mustBe "You cannot add any more supporting documents to all items. To add another, you need to remove one first. You can, however, still add a transport document to your items."
              result.allowMore mustBe true
          }
        }

        "when all documents reached max limit" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val maxDocLimit  = frontendAppConfig.maxSupportingDocuments + frontendAppConfig.maxTransportDocuments
              val supportDoc   = arbitrary[DocumentType](arbitrarySupportDocument).sample.value
              val transportDoc = arbitrary[DocumentType](arbitraryTransportDocument).sample.value

              val maxSupportDocs = (0 until maxDocLimit).foldLeft(emptyUserAnswers) {
                (answers, index) =>
                  answers.setValue(TypePage(Index(index)), supportDoc)
              }

              val maxDocs = (maxDocLimit until maxDocLimit * 2).foldLeft(maxSupportDocs) {
                (answers, index) =>
                  answers.setValue(TypePage(Index(index)), transportDoc)
              }
              val userAnswers = maxDocs

              val result = new AddAnotherDocumentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.allowMore mustBe false
          }
        }

        "when there are multiple documents" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(Index(0)), DocumentType(Support, "code1", "description1"))
                .setValue(DocumentReferenceNumberPage(Index(0)), "ref1")
                .setValue(TypePage(Index(1)), DocumentType(Transport, "code2", "description2"))
                .setValue(DocumentReferenceNumberPage(Index(1)), "ref2")

              val result = new AddAnotherDocumentViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems.length mustBe 2
              result.listItems.head.name mustBe "Support - ref1"
              result.listItems.last.name mustBe "Transport - ref2"
              result.title mustBe "You have added 2 documents to all items"
              result.heading mustBe "You have added 2 documents to all items"
              result.legend mustBe "Do you want to add another document to all items?"
              result.maxLimitLabel mustBe "You cannot add any more documents to all items. You can still add and attach documents to individual items from their house consignment cross-check screens."

              result.nextIndex mustBe Index(2)
          }
        }

        "ignores previous document type" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(Index(0)), DocumentType(Previous, "code1", "description1"))
                .setValue(DocumentReferenceNumberPage(Index(0)), "ref1")

              val result = new AddAnotherDocumentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems mustBe Nil
              result.title mustBe "You have added 0 document to all items"
              result.heading mustBe "You have added 0 document to all items"
              result.legend mustBe "Do you want to add a document to all items?"
              result.maxLimitLabel mustBe "You cannot add any more documents to all items. You can still add and attach documents to individual items from their house consignment cross-check screens."
              result.nextIndex mustBe Index(1)
          }
        }

        "when one has been removed" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(Index(0)), DocumentType(Support, "code1", "description1"))
                .setValue(DocumentReferenceNumberPage(Index(0)), "ref1")
                .setSequenceNumber(DocumentSection(Index(1)), "2") // keep only sequence number for the removed item (index 1 for this example)
                .setValue(TypePage(Index(2)), DocumentType(Transport, "code2", "description2"))
                .setValue(DocumentReferenceNumberPage(Index(2)), "ref2")

              val result = new AddAnotherDocumentViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems.length mustBe 2
              result.title mustBe "You have added 2 documents to all items"
              result.heading mustBe "You have added 2 documents to all items"
              result.legend mustBe "Do you want to add another document to all items?"
              result.maxLimitLabel mustBe "You cannot add any more documents to all items. You can still add and attach documents to individual items from their house consignment cross-check screens."
              result.nextIndex mustBe Index(3) // take 'removed item' into account when calculating the next index
          }
        }

        "and show change and remove links" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(Index(0)), DocumentType(Support, "code1", "description1"))
                .setValue(DocumentReferenceNumberPage(Index(0)), "ref1")
                .setValue(TypePage(Index(1)), DocumentType(Transport, "code2", "description2"))
                .setValue(DocumentReferenceNumberPage(Index(1)), "ref2")

              val result = new AddAnotherDocumentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = "Support - ref1",
                  changeUrl = Some("#"), //TODO: To be added later
                  removeUrl = Some(
                    controllers.documents.routes.RemoveDocumentYesNoController
                      .onPageLoad(arrivalId, mode, Index(0))
                      .url
                  )
                ),
                ListItem(
                  name = "Transport - ref2",
                  changeUrl = Some("#"), //TODO: To be added later
                  removeUrl = Some(
                    controllers.documents.routes.RemoveDocumentYesNoController
                      .onPageLoad(arrivalId, mode, Index(1))
                      .url
                  )
                )
              )

              result.nextIndex mustBe Index(2)
          }
        }
      }
    }
  }
}
