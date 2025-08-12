/*
 * Copyright 2023 HM Revenue & Customs
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

package services

import base.SpecBase
import models.DocType.{Support, Transport}
import models.reference.DocumentType
import models.{CheckMode, NormalMode, SelectableList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.documents.TypePage
import pages.houseConsignment.index.items.document.TypePage as ItemDocTypePage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DocumentsServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]
  private val service                                        = new DocumentsService(mockReferenceDataService)

  private val transportDocument1  = DocumentType(Transport, "N235", "Container list")
  private val transportDocument2  = DocumentType(Transport, "N741", "Master airwaybill")
  private val supportingDocument1 = DocumentType(Support, "C673", "Catch certificate")
  private val supportingDocument2 = DocumentType(Support, "N941", "Embargo permit")

  private val transportDocuments  = Seq(transportDocument1, transportDocument2)
  private val supportingDocuments = Seq(supportingDocument1, supportingDocument2)

  private val documents = transportDocuments ++ supportingDocuments

  "DocumentsService" - {

    "getDocumentList" - {
      "when consignment level" - {
        "and in Check Mode" - {
          "returns a list of list of transport and supporting documents when no document type selected previously" in {
            when(mockReferenceDataService.getDocuments()(any()))
              .thenReturn(Future.successful(documents))

            service.getDocumentList(emptyUserAnswers, documentIndex, CheckMode).futureValue mustEqual
              SelectableList(documents)
          }

          "returns a list of list of transport documents when a transport document type selected previously" in {
            when(mockReferenceDataService.getTransportDocuments()(any()))
              .thenReturn(Future.successful(transportDocuments))

            val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), transportDocument2)

            service.getDocumentList(userAnswers, documentIndex, CheckMode).futureValue mustEqual
              SelectableList(transportDocuments)
          }

          "returns a list of list of supporting documents when a supporting document type selected previously" in {
            when(mockReferenceDataService.getSupportingDocuments()(any()))
              .thenReturn(Future.successful(supportingDocuments))

            val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), supportingDocument2)

            service.getDocumentList(userAnswers, documentIndex, CheckMode).futureValue mustEqual
              SelectableList(supportingDocuments)
          }
        }
        "and in Normal Mode" - {
          "returns a list of transport and supporting documents" in {
            when(mockReferenceDataService.getDocuments()(any()))
              .thenReturn(Future.successful(documents))

            service.getDocumentList(emptyUserAnswers, documentIndex, NormalMode).futureValue mustEqual
              SelectableList(documents)
          }
        }
      }

      "when house consignment level" - {
        "and in Check Mode" - {
          "returns a list of list of transport and supporting documents when no document type selected previously" in {
            when(mockReferenceDataService.getDocuments()(any()))
              .thenReturn(Future.successful(documents))

            service.getDocumentList(emptyUserAnswers, houseConsignmentIndex, itemIndex, documentIndex, CheckMode).futureValue mustEqual
              SelectableList(documents)
          }

          "returns a list of list of transport documents when a transport document type selected previously" in {
            when(mockReferenceDataService.getTransportDocuments()(any()))
              .thenReturn(Future.successful(transportDocuments))

            val userAnswers = emptyUserAnswers.setValue(ItemDocTypePage(houseConsignmentIndex, itemIndex, documentIndex), transportDocument2)

            service.getDocumentList(userAnswers, houseConsignmentIndex, itemIndex, documentIndex, CheckMode).futureValue mustEqual
              SelectableList(transportDocuments)
          }

          "returns a list of list of supporting documents when a supporting document type selected previously" in {

            import pages.houseConsignment.index.documents.TypePage

            when(mockReferenceDataService.getSupportingDocuments()(any()))
              .thenReturn(Future.successful(supportingDocuments))

            val userAnswers = emptyUserAnswers.setValue(TypePage(houseConsignmentIndex, documentIndex), supportingDocument2)

            service.getDocumentList(userAnswers, houseConsignmentIndex, documentIndex, CheckMode).futureValue mustEqual
              SelectableList(supportingDocuments)
          }

          "returns a list of list of transport and supporting documents when no document type selected previously (non item)" in {

            when(mockReferenceDataService.getDocuments()(any()))
              .thenReturn(Future.successful(documents))

            service.getDocumentList(emptyUserAnswers, houseConsignmentIndex, documentIndex, CheckMode).futureValue mustEqual
              SelectableList(documents)
          }

          "returns a list of list of transport documents when a transport document type selected previously (non item)" in {

            import pages.houseConsignment.index.documents.TypePage

            when(mockReferenceDataService.getTransportDocuments()(any()))
              .thenReturn(Future.successful(transportDocuments))

            val userAnswers = emptyUserAnswers.setValue(TypePage(houseConsignmentIndex, documentIndex), transportDocument2)

            service.getDocumentList(userAnswers, houseConsignmentIndex, documentIndex, CheckMode).futureValue mustEqual
              SelectableList(transportDocuments)
          }

          "returns a list of list of supporting documents when a supporting document type selected previously (non item)" in {

            import pages.houseConsignment.index.documents.TypePage

            when(mockReferenceDataService.getSupportingDocuments()(any()))
              .thenReturn(Future.successful(supportingDocuments))

            val userAnswers = emptyUserAnswers.setValue(TypePage(houseConsignmentIndex, documentIndex), supportingDocument2)

            service.getDocumentList(userAnswers, houseConsignmentIndex, documentIndex, CheckMode).futureValue mustEqual
              SelectableList(supportingDocuments)
          }
        }

        "and in NormalMode" - {
          "returns a list of transport and supporting documents" in {
            when(mockReferenceDataService.getDocuments()(any()))
              .thenReturn(Future.successful(documents))

            service.getDocumentList(emptyUserAnswers, houseConsignmentIndex, itemIndex, documentIndex, NormalMode).futureValue mustEqual
              SelectableList(documents)
          }
        }
      }
    }
  }
}
