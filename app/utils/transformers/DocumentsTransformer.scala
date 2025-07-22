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

import generated.*
import models.Document.*
import models.{Documents, Index, UserAnswers}
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DocumentsTransformer @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(
    supportingDocuments: Seq[SupportingDocumentType02],
    transportDocuments: Seq[TransportDocumentType01],
    previousDocuments: Seq[PreviousDocumentType05]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.documents.*
    import pages.sections.documents.DocumentSection

    val documents = Documents(
      supportingDocuments = supportingDocuments.map(SupportingDocument(_)),
      transportDocuments = transportDocuments.map(TransportDocument(_)),
      previousDocuments = previousDocuments.map(PreviousDocument(_))
    )

    documents.documents.mapWithSets {
      case (SupportingDocument(sequenceNumber, typeValue, referenceNumber, complementOfInformation), documentIndex) =>
        setSequenceNumber(DocumentSection(documentIndex), sequenceNumber) andThen
          set(TypePage(documentIndex), typeValue, referenceDataService.getSupportingDocument) andThen
          set(DocumentReferenceNumberPage(documentIndex), referenceNumber) andThen
          set(AdditionalInformationPage(documentIndex), complementOfInformation)
      case (TransportDocument(sequenceNumber, typeValue, referenceNumber), documentIndex) =>
        setSequenceNumber(DocumentSection(documentIndex), sequenceNumber) andThen
          set(TypePage(documentIndex), typeValue, referenceDataService.getTransportDocument) andThen
          set(DocumentReferenceNumberPage(documentIndex), referenceNumber)
      case (PreviousDocument(sequenceNumber, typeValue, referenceNumber, complementOfInformation), documentIndex) =>
        setSequenceNumber(DocumentSection(documentIndex), sequenceNumber) andThen
          set(TypePage(documentIndex), typeValue, referenceDataService.getPreviousDocument) andThen
          set(DocumentReferenceNumberPage(documentIndex), referenceNumber) andThen
          set(AdditionalInformationPage(documentIndex), complementOfInformation)
    }
  }

  def transform(
    supportingDocuments: Seq[SupportingDocumentType02],
    transportDocuments: Seq[TransportDocumentType01],
    previousDocuments: Seq[PreviousDocumentType03],
    hcIndex: Index,
    itemIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.items.document.*
    import pages.sections.houseConsignment.index.items.documents.DocumentSection

    val documents = Documents(
      supportingDocuments = supportingDocuments.map(SupportingDocument(_)),
      transportDocuments = transportDocuments.map(TransportDocument(_)),
      previousDocuments = previousDocuments.map(PreviousDocument(_))
    )

    documents.documents.mapWithSets {
      case (SupportingDocument(sequenceNumber, typeValue, referenceNumber, complementOfInformation), documentIndex) =>
        setSequenceNumber(DocumentSection(hcIndex, itemIndex, documentIndex), sequenceNumber) andThen
          set(TypePage(hcIndex, itemIndex, documentIndex), typeValue, referenceDataService.getSupportingDocument) andThen
          set(DocumentReferenceNumberPage(hcIndex, itemIndex, documentIndex), referenceNumber) andThen
          set(AdditionalInformationPage(hcIndex, itemIndex, documentIndex), complementOfInformation)
      case (TransportDocument(sequenceNumber, typeValue, referenceNumber), documentIndex) =>
        setSequenceNumber(DocumentSection(hcIndex, itemIndex, documentIndex), sequenceNumber) andThen
          set(TypePage(hcIndex, itemIndex, documentIndex), typeValue, referenceDataService.getTransportDocument) andThen
          set(DocumentReferenceNumberPage(hcIndex, itemIndex, documentIndex), referenceNumber)
      case (PreviousDocument(sequenceNumber, typeValue, referenceNumber, complementOfInformation), documentIndex) =>
        setSequenceNumber(DocumentSection(hcIndex, itemIndex, documentIndex), sequenceNumber) andThen
          set(TypePage(hcIndex, itemIndex, documentIndex), typeValue, referenceDataService.getPreviousDocument) andThen
          set(DocumentReferenceNumberPage(hcIndex, itemIndex, documentIndex), referenceNumber) andThen
          set(AdditionalInformationPage(hcIndex, itemIndex, documentIndex), complementOfInformation)
    }
  }

  def transform(
    supportingDocuments: Seq[SupportingDocumentType02],
    transportDocuments: Seq[TransportDocumentType01],
    previousDocuments: Seq[PreviousDocumentType06],
    hcIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.documents.*
    import pages.sections.houseConsignment.index.documents.DocumentSection

    val documents = Documents(
      supportingDocuments = supportingDocuments.map(SupportingDocument(_)),
      transportDocuments = transportDocuments.map(TransportDocument(_)),
      previousDocuments = previousDocuments.map(PreviousDocument(_))
    )

    documents.documents.mapWithSets {
      case (SupportingDocument(sequenceNumber, typeValue, referenceNumber, complementOfInformation), documentIndex) =>
        setSequenceNumber(DocumentSection(hcIndex, documentIndex), sequenceNumber) andThen
          set(TypePage(hcIndex, documentIndex), typeValue, referenceDataService.getSupportingDocument) andThen
          set(DocumentReferenceNumberPage(hcIndex, documentIndex), referenceNumber) andThen
          set(AdditionalInformationPage(hcIndex, documentIndex), complementOfInformation)
      case (TransportDocument(sequenceNumber, typeValue, referenceNumber), documentIndex) =>
        setSequenceNumber(DocumentSection(hcIndex, documentIndex), sequenceNumber) andThen
          set(TypePage(hcIndex, documentIndex), typeValue, referenceDataService.getTransportDocument) andThen
          set(DocumentReferenceNumberPage(hcIndex, documentIndex), referenceNumber)
      case (PreviousDocument(sequenceNumber, typeValue, referenceNumber, complementOfInformation), documentIndex) =>
        setSequenceNumber(DocumentSection(hcIndex, documentIndex), sequenceNumber) andThen
          set(TypePage(hcIndex, documentIndex), typeValue, referenceDataService.getPreviousDocumentExport) andThen
          set(DocumentReferenceNumberPage(hcIndex, documentIndex), referenceNumber) andThen
          set(AdditionalInformationPage(hcIndex, documentIndex), complementOfInformation)
    }
  }
}
