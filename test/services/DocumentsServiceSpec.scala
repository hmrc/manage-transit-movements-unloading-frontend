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

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import models.DocType.{Support, Transport}
import models.reference.DocumentType
import models.{CheckMode, NormalMode, SelectableList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.documents.TypePage
import pages.houseConsignment.index.items.document.{TypePage => ItemDocTypePage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DocumentsServiceSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new DocumentsService(mockRefDataConnector)

  private val transportDocument1  = DocumentType(Transport, "N235", "Container list")
  private val transportDocument2  = DocumentType(Transport, "N741", "Master airwaybill")
  private val supportingDocument1 = DocumentType(Support, "C673", "Catch certificate")
  private val supportingDocument2 = DocumentType(Support, "N941", "Embargo permit")

  private val transportDocuments  = NonEmptySet.of(transportDocument1, transportDocument2)
  private val supportingDocuments = NonEmptySet.of(supportingDocument1, supportingDocument2)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "DocumentsService" - {

    "getDocuments" - {
      "returns a list of transport and supporting documents" in {
        when(mockRefDataConnector.getTransportDocuments()(any(), any()))
          .thenReturn(Future.successful(transportDocuments))
        when(mockRefDataConnector.getSupportingDocuments()(any(), any()))
          .thenReturn(Future.successful(supportingDocuments))

        service.getDocuments().futureValue mustBe
          SelectableList(Seq(supportingDocument1, transportDocument1, transportDocument2, supportingDocument2))

        verify(mockRefDataConnector).getTransportDocuments()(any(), any())
        verify(mockRefDataConnector).getSupportingDocuments()(any(), any())
      }
    }

    "getTransportDocuments" - {
      "returns a list of transport documents" in {
        when(mockRefDataConnector.getTransportDocuments()(any(), any()))
          .thenReturn(Future.successful(transportDocuments))

        service.getTransportDocuments().futureValue mustBe
          SelectableList(Seq(transportDocument1, transportDocument2))

        verify(mockRefDataConnector).getTransportDocuments()(any(), any())
      }
    }

    "getSupportingDocuments" - {
      "returns a list of supporting documents" in {
        when(mockRefDataConnector.getSupportingDocuments()(any(), any()))
          .thenReturn(Future.successful(supportingDocuments))

        service.getSupportingDocuments().futureValue mustBe
          SelectableList(Seq(supportingDocument1, supportingDocument2))

        verify(mockRefDataConnector).getSupportingDocuments()(any(), any())
      }
    }

    "getDocumentList" - {
      "when consignment level" - {
        "and in Check Mode" - {
          "returns a list of list of transport and supporting documents when no document type selected previously" in {
            when(mockRefDataConnector.getTransportDocuments()(any(), any()))
              .thenReturn(Future.successful(transportDocuments))
            when(mockRefDataConnector.getSupportingDocuments()(any(), any()))
              .thenReturn(Future.successful(supportingDocuments))

            service.getDocumentList(emptyUserAnswers, documentIndex, CheckMode).futureValue mustBe
              SelectableList(Seq(supportingDocument1, transportDocument1, transportDocument2, supportingDocument2))
          }

          "returns a list of list of transport documents when a transport document type selected previously" in {
            when(mockRefDataConnector.getTransportDocuments()(any(), any()))
              .thenReturn(Future.successful(transportDocuments))

            val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), transportDocument2)

            service.getDocumentList(userAnswers, documentIndex, CheckMode).futureValue mustBe
              SelectableList(Seq(transportDocument1, transportDocument2))
          }

          "returns a list of list of supporting documents when a supporting document type selected previously" in {
            when(mockRefDataConnector.getSupportingDocuments()(any(), any()))
              .thenReturn(Future.successful(supportingDocuments))

            val userAnswers = emptyUserAnswers.setValue(TypePage(documentIndex), supportingDocument2)

            service.getDocumentList(userAnswers, documentIndex, CheckMode).futureValue mustBe
              SelectableList(Seq(supportingDocument1, supportingDocument2))
          }
        }
        "and in Normal Mode" - {
          "returns a list of transport and supporting documents" in {
            when(mockRefDataConnector.getTransportDocuments()(any(), any()))
              .thenReturn(Future.successful(transportDocuments))
            when(mockRefDataConnector.getSupportingDocuments()(any(), any()))
              .thenReturn(Future.successful(supportingDocuments))

            service.getDocumentList(emptyUserAnswers, documentIndex, NormalMode).futureValue mustBe
              SelectableList(Seq(supportingDocument1, transportDocument1, transportDocument2, supportingDocument2))
          }
        }
      }

      "when house consignment level" - {
        "returns a list of list of transport and supporting documents when no document type selected previously" in {
          when(mockRefDataConnector.getTransportDocuments()(any(), any()))
            .thenReturn(Future.successful(transportDocuments))
          when(mockRefDataConnector.getSupportingDocuments()(any(), any()))
            .thenReturn(Future.successful(supportingDocuments))

          service.getDocumentList(emptyUserAnswers, houseConsignmentIndex, itemIndex, documentIndex).futureValue mustBe
            SelectableList(Seq(supportingDocument1, transportDocument1, transportDocument2, supportingDocument2))
        }

        "returns a list of list of transport documents when a transport document type selected previously" in {
          when(mockRefDataConnector.getTransportDocuments()(any(), any()))
            .thenReturn(Future.successful(transportDocuments))

          val userAnswers = emptyUserAnswers.setValue(ItemDocTypePage(houseConsignmentIndex, itemIndex, documentIndex), transportDocument2)

          service.getDocumentList(userAnswers, houseConsignmentIndex, itemIndex, documentIndex).futureValue mustBe
            SelectableList(Seq(transportDocument1, transportDocument2))
        }

        "returns a list of list of supporting documents when a supporting document type selected previously" in {
          when(mockRefDataConnector.getSupportingDocuments()(any(), any()))
            .thenReturn(Future.successful(supportingDocuments))

          val userAnswers = emptyUserAnswers.setValue(ItemDocTypePage(houseConsignmentIndex, itemIndex, documentIndex), supportingDocument2)

          service.getDocumentList(userAnswers, houseConsignmentIndex, itemIndex, documentIndex).futureValue mustBe
            SelectableList(Seq(supportingDocument1, supportingDocument2))
        }
      }
    }
  }
}
