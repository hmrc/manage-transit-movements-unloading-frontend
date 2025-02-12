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

package utils.transformers

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.{PreviousDocumentType06, SupportingDocumentType02, TransportDocumentType02}
import generators.Generators
import models.DocType.{Previous, Support, Transport}
import models.Index
import models.reference.DocumentType
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService

import scala.concurrent.Future

class DocumentsTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[DocumentsTransformer]

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataService].toInstance(mockReferenceDataService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  "must transform data" - {
    "when consignment documents" in {
      import pages.documents.*
      import pages.sections.documents.DocumentSection

      val supportingDocuments = Seq(
        SupportingDocumentType02(
          sequenceNumber = 1,
          typeValue = "sd1 tv",
          referenceNumber = "sd1 rn",
          complementOfInformation = Some("sd1 coi")
        ),
        SupportingDocumentType02(
          sequenceNumber = 2,
          typeValue = "sd2 tv",
          referenceNumber = "sd2 rn",
          complementOfInformation = None
        )
      )

      val previousDocuments = Seq(
        PreviousDocumentType06(
          sequenceNumber = 1,
          typeValue = "pd1 tv",
          referenceNumber = "pd1 rn",
          complementOfInformation = Some("pd1 coi")
        ),
        PreviousDocumentType06(
          sequenceNumber = 2,
          typeValue = "pd2 tv",
          referenceNumber = "pd2 rn",
          complementOfInformation = None
        )
      )

      val transportDocuments = Seq(
        TransportDocumentType02(
          sequenceNumber = 1,
          typeValue = "td1 tv",
          referenceNumber = "td1 rn"
        ),
        TransportDocumentType02(
          sequenceNumber = 2,
          typeValue = "td2 tv",
          referenceNumber = "td2 rn"
        )
      )

      when(mockReferenceDataService.getSupportingDocument(eqTo("sd1 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Support, "sd1 tv", "sd1 d")))

      when(mockReferenceDataService.getSupportingDocument(eqTo("sd2 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Support, "sd2 tv", "sd2 d")))

      when(mockReferenceDataService.getTransportDocument(eqTo("td1 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Transport, "td1 tv", "td1 d")))

      when(mockReferenceDataService.getTransportDocument(eqTo("td2 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Transport, "td2 tv", "td2 d")))

      when(mockReferenceDataService.getPreviousDocument(eqTo("pd1 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Previous, "pd1 tv", "pd1 d")))

      when(mockReferenceDataService.getPreviousDocument(eqTo("pd2 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Previous, "pd2 tv", "pd2 d")))

      val result = transformer.transform(supportingDocuments, transportDocuments, previousDocuments).apply(emptyUserAnswers).futureValue

      result.getSequenceNumber(DocumentSection(Index(0))) mustBe 1
      result.getValue(TypePage(Index(0))).toString mustBe "Supporting - (sd1 tv) sd1 d"
      result.getValue(DocumentReferenceNumberPage(Index(0))) mustBe "sd1 rn"
      result.getValue(AdditionalInformationPage(Index(0))) mustBe "sd1 coi"

      result.getSequenceNumber(DocumentSection(Index(1))) mustBe 2
      result.getValue(TypePage(Index(1))).toString mustBe "Supporting - (sd2 tv) sd2 d"
      result.getValue(DocumentReferenceNumberPage(Index(1))) mustBe "sd2 rn"
      result.get(AdditionalInformationPage(Index(1))) must not be defined

      result.getSequenceNumber(DocumentSection(Index(2))) mustBe 1
      result.getValue(TypePage(Index(2))).toString mustBe "Transport - (td1 tv) td1 d"
      result.getValue(DocumentReferenceNumberPage(Index(2))) mustBe "td1 rn"
      result.get(AdditionalInformationPage(Index(2))) must not be defined

      result.getSequenceNumber(DocumentSection(Index(3))) mustBe 2
      result.getValue(TypePage(Index(3))).toString mustBe "Transport - (td2 tv) td2 d"
      result.getValue(DocumentReferenceNumberPage(Index(3))) mustBe "td2 rn"
      result.get(AdditionalInformationPage(Index(3))) must not be defined

      result.getSequenceNumber(DocumentSection(Index(4))) mustBe 1
      result.getValue(TypePage(Index(4))).toString mustBe "Previous - (pd1 tv) pd1 d"
      result.getValue(DocumentReferenceNumberPage(Index(4))) mustBe "pd1 rn"
      result.getValue(AdditionalInformationPage(Index(4))) mustBe "pd1 coi"

      result.getSequenceNumber(DocumentSection(Index(5))) mustBe 2
      result.getValue(TypePage(Index(5))).toString mustBe "Previous - (pd2 tv) pd2 d"
      result.getValue(DocumentReferenceNumberPage(Index(5))) mustBe "pd2 rn"
      result.get(AdditionalInformationPage(Index(5))) must not be defined
    }

    "when consignment item documents" in {
      import pages.houseConsignment.index.items.document.*
      import pages.sections.houseConsignment.index.items.documents.DocumentSection

      val supportingDocuments = Seq(
        SupportingDocumentType02(
          sequenceNumber = 1,
          typeValue = "sd1 tv",
          referenceNumber = "sd1 rn",
          complementOfInformation = Some("sd1 coi")
        ),
        SupportingDocumentType02(
          sequenceNumber = 2,
          typeValue = "sd2 tv",
          referenceNumber = "sd2 rn",
          complementOfInformation = None
        )
      )

      val transportDocuments = Seq(
        TransportDocumentType02(
          sequenceNumber = 1,
          typeValue = "td1 tv",
          referenceNumber = "td1 rn"
        ),
        TransportDocumentType02(
          sequenceNumber = 2,
          typeValue = "td2 tv",
          referenceNumber = "td2 rn"
        )
      )

      val previousDocuments = Seq(
        PreviousDocumentType06(
          sequenceNumber = 1,
          typeValue = "pd1 tv",
          referenceNumber = "pd1 rn",
          complementOfInformation = Some("pd1 coi")
        ),
        PreviousDocumentType06(
          sequenceNumber = 2,
          typeValue = "pd2 tv",
          referenceNumber = "pd2 rn",
          complementOfInformation = None
        )
      )

      when(mockReferenceDataService.getSupportingDocument(eqTo("sd1 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Support, "sd1 tv", "sd1 d")))

      when(mockReferenceDataService.getSupportingDocument(eqTo("sd2 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Support, "sd2 tv", "sd2 d")))

      when(mockReferenceDataService.getTransportDocument(eqTo("td1 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Transport, "td1 tv", "td1 d")))

      when(mockReferenceDataService.getTransportDocument(eqTo("td2 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Transport, "td2 tv", "td2 d")))

      when(mockReferenceDataService.getPreviousDocument(eqTo("pd1 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Previous, "pd1 tv", "pd1 d")))

      when(mockReferenceDataService.getPreviousDocument(eqTo("pd2 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Previous, "pd2 tv", "pd2 d")))

      val result = transformer.transform(supportingDocuments, transportDocuments, previousDocuments, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

      result.getSequenceNumber(DocumentSection(hcIndex, itemIndex, Index(0))) mustBe 1
      result.getValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(0))) mustBe "sd1 rn"
      result.getValue(AdditionalInformationPage(hcIndex, itemIndex, Index(0))) mustBe "sd1 coi"
      result.getValue(TypePage(hcIndex, itemIndex, Index(0))).toString mustBe "Supporting - (sd1 tv) sd1 d"

      result.getSequenceNumber(DocumentSection(hcIndex, itemIndex, Index(1))) mustBe 2
      result.getValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(1))) mustBe "sd2 rn"
      result.get(AdditionalInformationPage(hcIndex, itemIndex, Index(1))) must not be defined
      result.getValue(TypePage(hcIndex, itemIndex, Index(1))).toString mustBe "Supporting - (sd2 tv) sd2 d"

      result.getSequenceNumber(DocumentSection(hcIndex, itemIndex, Index(2))) mustBe 1
      result.getValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(2))) mustBe "td1 rn"
      result.get(AdditionalInformationPage(hcIndex, itemIndex, Index(2))) must not be defined
      result.getValue(TypePage(hcIndex, itemIndex, Index(2))).toString mustBe "Transport - (td1 tv) td1 d"

      result.getSequenceNumber(DocumentSection(hcIndex, itemIndex, Index(3))) mustBe 2
      result.getValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(3))) mustBe "td2 rn"
      result.get(AdditionalInformationPage(hcIndex, itemIndex, Index(3))) must not be defined
      result.getValue(TypePage(hcIndex, itemIndex, Index(3))).toString mustBe "Transport - (td2 tv) td2 d"

      result.getValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(4))) mustBe "pd1 rn"
      result.getValue(AdditionalInformationPage(hcIndex, itemIndex, Index(4))) mustBe "pd1 coi"
      result.getValue(TypePage(hcIndex, itemIndex, Index(4))).toString mustBe "Previous - (pd1 tv) pd1 d"

      result.getValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(5))) mustBe "pd2 rn"
      result.get(AdditionalInformationPage(hcIndex, itemIndex, Index(5))) must not be defined
      result.getValue(TypePage(hcIndex, itemIndex, Index(5))).toString mustBe "Previous - (pd2 tv) pd2 d"
    }

    "when house consignment documents" in {
      import pages.houseConsignment.index.documents.*
      import pages.sections.houseConsignment.index.documents.DocumentSection

      val supportingDocuments = Seq(
        SupportingDocumentType02(
          sequenceNumber = 1,
          typeValue = "sd1 tv",
          referenceNumber = "sd1 rn",
          complementOfInformation = Some("sd1 coi")
        ),
        SupportingDocumentType02(
          sequenceNumber = 2,
          typeValue = "sd2 tv",
          referenceNumber = "sd2 rn",
          complementOfInformation = None
        )
      )

      val transportDocuments = Seq(
        TransportDocumentType02(
          sequenceNumber = 1,
          typeValue = "td1 tv",
          referenceNumber = "td1 rn"
        ),
        TransportDocumentType02(
          sequenceNumber = 2,
          typeValue = "td2 tv",
          referenceNumber = "td2 rn"
        )
      )

      val previousDocuments = Seq(
        PreviousDocumentType06(
          sequenceNumber = 1,
          typeValue = "pd1 tv",
          referenceNumber = "pd1 rn",
          complementOfInformation = Some("pd1 coi")
        ),
        PreviousDocumentType06(
          sequenceNumber = 2,
          typeValue = "pd2 tv",
          referenceNumber = "pd2 rn",
          complementOfInformation = None
        )
      )

      when(mockReferenceDataService.getSupportingDocument(eqTo("sd1 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Support, "sd1 tv", "sd1 d")))

      when(mockReferenceDataService.getSupportingDocument(eqTo("sd2 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Support, "sd2 tv", "sd2 d")))

      when(mockReferenceDataService.getTransportDocument(eqTo("td1 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Transport, "td1 tv", "td1 d")))

      when(mockReferenceDataService.getTransportDocument(eqTo("td2 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Transport, "td2 tv", "td2 d")))

      when(mockReferenceDataService.getPreviousDocumentExport(eqTo("pd1 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Previous, "pd1 tv", "pd1 d")))

      when(mockReferenceDataService.getPreviousDocumentExport(eqTo("pd2 tv"))(any()))
        .thenReturn(Future.successful(DocumentType(Previous, "pd2 tv", "pd2 d")))

      val result = transformer.transform(supportingDocuments, transportDocuments, previousDocuments, hcIndex).apply(emptyUserAnswers).futureValue

      result.getSequenceNumber(DocumentSection(hcIndex, Index(0))) mustBe 1
      result.getValue(DocumentReferenceNumberPage(hcIndex, Index(0))) mustBe "sd1 rn"
      result.getValue(AdditionalInformationPage(hcIndex, Index(0))) mustBe "sd1 coi"
      result.getValue(TypePage(hcIndex, Index(0))).toString mustBe "Supporting - (sd1 tv) sd1 d"

      result.getSequenceNumber(DocumentSection(hcIndex, Index(1))) mustBe 2
      result.getValue(DocumentReferenceNumberPage(hcIndex, Index(1))) mustBe "sd2 rn"
      result.get(AdditionalInformationPage(hcIndex, Index(1))) must not be defined
      result.getValue(TypePage(hcIndex, Index(1))).toString mustBe "Supporting - (sd2 tv) sd2 d"

      result.getSequenceNumber(DocumentSection(hcIndex, Index(2))) mustBe 1
      result.getValue(DocumentReferenceNumberPage(hcIndex, Index(2))) mustBe "td1 rn"
      result.get(AdditionalInformationPage(hcIndex, Index(2))) must not be defined
      result.getValue(TypePage(hcIndex, Index(2))).toString mustBe "Transport - (td1 tv) td1 d"

      result.getSequenceNumber(DocumentSection(hcIndex, Index(3))) mustBe 2
      result.getValue(DocumentReferenceNumberPage(hcIndex, Index(3))) mustBe "td2 rn"
      result.get(AdditionalInformationPage(hcIndex, Index(3))) must not be defined
      result.getValue(TypePage(hcIndex, Index(3))).toString mustBe "Transport - (td2 tv) td2 d"

      result.getValue(DocumentReferenceNumberPage(hcIndex, Index(4))) mustBe "pd1 rn"
      result.getValue(AdditionalInformationPage(hcIndex, Index(4))) mustBe "pd1 coi"
      result.getValue(TypePage(hcIndex, Index(4))).toString mustBe "Previous - (pd1 tv) pd1 d"

      result.getValue(DocumentReferenceNumberPage(hcIndex, Index(5))) mustBe "pd2 rn"
      result.get(AdditionalInformationPage(hcIndex, Index(5))) must not be defined
      result.getValue(TypePage(hcIndex, Index(5))).toString mustBe "Previous - (pd2 tv) pd2 d"
    }
  }
}
