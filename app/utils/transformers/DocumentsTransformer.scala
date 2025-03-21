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

import generated.{PreviousDocumentType06, SupportingDocumentType02, TransportDocumentType02}
import models.reference.DocumentType
import models.{Document, Index, UserAnswers}
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
    transportDocuments: Seq[TransportDocumentType02],
    previousDocuments: Seq[PreviousDocumentType06]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.documents.*
    import pages.sections.documents.DocumentSection

    genericTransform(supportingDocuments, transportDocuments, previousDocuments) {
      previousDocument =>
        referenceDataService.getPreviousDocument(previousDocument.typeValue)
    } {
      case (document, documentIndex) =>
        document match {
          case Document.SupportingDocument(sequenceNumber, documentType, referenceNumber, complementOfInformation) =>
            setSequenceNumber(DocumentSection(documentIndex), sequenceNumber) andThen
              set(TypePage(documentIndex), documentType) andThen
              set(DocumentReferenceNumberPage(documentIndex), referenceNumber) andThen
              set(AdditionalInformationPage(documentIndex), complementOfInformation)
          case Document.TransportDocument(sequenceNumber, documentType, referenceNumber) =>
            setSequenceNumber(DocumentSection(documentIndex), sequenceNumber) andThen
              set(TypePage(documentIndex), documentType) andThen
              set(DocumentReferenceNumberPage(documentIndex), referenceNumber)
          case Document.PreviousDocument(sequenceNumber, documentType, referenceNumber, complementOfInformation) =>
            setSequenceNumber(DocumentSection(documentIndex), sequenceNumber) andThen
              set(TypePage(documentIndex), documentType) andThen
              set(DocumentReferenceNumberPage(documentIndex), referenceNumber) andThen
              set(AdditionalInformationPage(documentIndex), complementOfInformation)
        }
    }
  }

  def transform(
    supportingDocuments: Seq[SupportingDocumentType02],
    transportDocuments: Seq[TransportDocumentType02],
    previousDocuments: Seq[PreviousDocumentType06],
    hcIndex: Index,
    itemIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.items.document.*
    import pages.sections.houseConsignment.index.items.documents.DocumentSection

    genericTransform(supportingDocuments, transportDocuments, previousDocuments) {
      previousDocument =>
        referenceDataService.getPreviousDocument(previousDocument.typeValue)
    } {
      case (document, documentIndex) =>
        document match {
          case Document.SupportingDocument(sequenceNumber, documentType, referenceNumber, complementOfInformation) =>
            setSequenceNumber(DocumentSection(hcIndex, itemIndex, documentIndex), sequenceNumber) andThen
              set(DocumentReferenceNumberPage(hcIndex, itemIndex, documentIndex), referenceNumber) andThen
              set(AdditionalInformationPage(hcIndex, itemIndex, documentIndex), complementOfInformation) andThen
              set(TypePage(hcIndex, itemIndex, documentIndex), documentType)
          case Document.TransportDocument(sequenceNumber, documentType, referenceNumber) =>
            setSequenceNumber(DocumentSection(hcIndex, itemIndex, documentIndex), sequenceNumber) andThen
              set(DocumentReferenceNumberPage(hcIndex, itemIndex, documentIndex), referenceNumber) andThen
              set(TypePage(hcIndex, itemIndex, documentIndex), documentType)
          case Document.PreviousDocument(sequenceNumber, documentType, referenceNumber, complementOfInformation) =>
            setSequenceNumber(DocumentSection(hcIndex, itemIndex, documentIndex), sequenceNumber) andThen
              set(DocumentReferenceNumberPage(hcIndex, itemIndex, documentIndex), referenceNumber) andThen
              set(AdditionalInformationPage(hcIndex, itemIndex, documentIndex), complementOfInformation) andThen
              set(TypePage(hcIndex, itemIndex, documentIndex), documentType)
        }
    }
  }

  def transform(
    supportingDocuments: Seq[SupportingDocumentType02],
    transportDocuments: Seq[TransportDocumentType02],
    previousDocuments: Seq[PreviousDocumentType06],
    hcIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.documents.*
    import pages.sections.houseConsignment.index.documents.DocumentSection

    genericTransform(supportingDocuments, transportDocuments, previousDocuments) {
      previousDocument =>
        referenceDataService.getPreviousDocumentExport(previousDocument.typeValue)
    } {
      case (document, documentIndex) =>
        document match {
          case Document.SupportingDocument(sequenceNumber, documentType, referenceNumber, complementOfInformation) =>
            setSequenceNumber(DocumentSection(hcIndex, documentIndex), sequenceNumber) andThen
              set(DocumentReferenceNumberPage(hcIndex, documentIndex), referenceNumber) andThen
              set(AdditionalInformationPage(hcIndex, documentIndex), complementOfInformation) andThen
              set(TypePage(hcIndex, documentIndex), documentType)
          case Document.TransportDocument(sequenceNumber, documentType, referenceNumber) =>
            setSequenceNumber(DocumentSection(hcIndex, documentIndex), sequenceNumber) andThen
              set(DocumentReferenceNumberPage(hcIndex, documentIndex), referenceNumber) andThen
              set(TypePage(hcIndex, documentIndex), documentType)
          case Document.PreviousDocument(sequenceNumber, documentType, referenceNumber, complementOfInformation) =>
            setSequenceNumber(DocumentSection(hcIndex, documentIndex), sequenceNumber) andThen
              set(DocumentReferenceNumberPage(hcIndex, documentIndex), referenceNumber) andThen
              set(AdditionalInformationPage(hcIndex, documentIndex), complementOfInformation) andThen
              set(TypePage(hcIndex, documentIndex), documentType)
        }
    }
  }

  private def genericTransform(
    supportingDocuments: Seq[SupportingDocumentType02],
    transportDocuments: Seq[TransportDocumentType02],
    previousDocuments: Seq[PreviousDocumentType06]
  )(
    previousDocumentLookup: PreviousDocumentType06 => Future[DocumentType]
  )(
    pipeline: (Document, Index) => UserAnswers => Future[UserAnswers]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {

    lazy val supportingDocumentsWithDescription = supportingDocuments.map {
      supportingDocument =>
        referenceDataService.getSupportingDocument(supportingDocument.typeValue).map {
          Document.apply(supportingDocument, _)
        }
    }

    lazy val transportDocumentsWithDescription = transportDocuments.map {
      transportDocument =>
        referenceDataService.getTransportDocument(transportDocument.typeValue).map {
          Document.apply(transportDocument, _)
        }
    }

    lazy val previousDocumentsWithDescription = previousDocuments.map {
      previousDocument =>
        previousDocumentLookup(previousDocument).map {
          Document.apply(previousDocument, _)
        }
    }

    for {
      sd <- Future.sequence(supportingDocumentsWithDescription)
      td <- Future.sequence(transportDocumentsWithDescription)
      pd <- Future.sequence(previousDocumentsWithDescription)
      documents = sd ++ td ++ pd
      userAnswers <- documents.zipWithIndex.foldLeft(Future.successful(userAnswers)) {
        case (acc, (document, i)) =>
          acc.flatMap(pipeline(document, Index(i)))
      }
    } yield userAnswers
  }
}
