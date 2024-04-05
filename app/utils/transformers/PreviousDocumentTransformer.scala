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

import connectors.ReferenceDataConnector
import generated.PreviousDocumentType07
import models.Document.PreviousDocument
import models._
import models.reference.DocumentType
import pages.houseConsignment.index.items.{DeclarationGoodsItemNumberPage, DeclarationTypePage}
import pages.houseConsignment.previousDocument.{AdditionalInformationPage, DocumentReferenceNumberPage, TypePage}
import pages.sections.ItemSection
import pages.sections.houseConsignment.index.previousDocument.PreviousDocumentSection
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) extends PageTransformer {

  def transform(previousDocuments: Seq[PreviousDocumentType07], hcIndex: Index)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    genericTransform(previousDocuments) {
      case (document, documentIndex) =>
        setSequenceNumber(PreviousDocumentSection(hcIndex, documentIndex), document.sequenceNumber) andThen
          set(DocumentReferenceNumberPage(hcIndex, documentIndex), document.referenceNumber) andThen
          set(AdditionalInformationPage(hcIndex, documentIndex), document.complementOfInformation) andThen
          set(TypePage(hcIndex, documentIndex), document.documentType)
    }

  private def genericTransform(
    previousDocuments: Seq[PreviousDocumentType07]
  )(
    pipeline: (PreviousDocument, Index) => UserAnswers => Future[UserAnswers]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {

    lazy val previousDocumentsWithDescription: Seq[Future[Document.PreviousDocument]] = previousDocuments.map {
      previousDocument =>
        referenceDataConnector.getPreviousDocument(previousDocument.typeValue).map {
          Document.apply(previousDocument, _)
        }
    }

    for {
      documents <- Future.sequence(previousDocumentsWithDescription)
      userAnswers <- documents.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (document, i)) =>
          acc.flatMap(pipeline(document, Index(i)))
      })
    } yield userAnswers
  }

}
