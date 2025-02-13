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

import models.reference.DocumentType
import models.{CheckMode, DocType, Index, Mode, SelectableList, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DocumentsService @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext) {

  def getDocumentList(
    userAnswers: UserAnswers,
    documentIndex: Index,
    mode: Mode
  )(implicit hc: HeaderCarrier): Future[SelectableList[DocumentType]] = {
    import pages.documents.TypePage

    val documentsF = if (mode == CheckMode) {
      userAnswers.get(TypePage(documentIndex)).map(_.`type`) match {
        case Some(DocType.Transport) => referenceDataService.getTransportDocuments()
        case Some(DocType.Support)   => referenceDataService.getSupportingDocuments()
        case _                       => referenceDataService.getDocuments()
      }
    } else {
      referenceDataService.getDocuments()
    }

    documentsF.map(SelectableList(_))
  }

  def getDocumentList(
    userAnswers: UserAnswers,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index,
    mode: Mode
  )(implicit hc: HeaderCarrier): Future[SelectableList[DocumentType]] = {
    import pages.houseConsignment.index.items.document.TypePage

    val documentsF = if (mode == CheckMode) {
      userAnswers.get(TypePage(houseConsignmentIndex, itemIndex, documentIndex)).map(_.`type`) match {
        case Some(DocType.Transport) => referenceDataService.getTransportDocuments()
        case Some(DocType.Support)   => referenceDataService.getSupportingDocuments()
        case _                       => referenceDataService.getDocuments()
      }
    } else {
      referenceDataService.getDocuments()
    }

    documentsF.map(SelectableList(_))
  }

  def getDocumentList(
    userAnswers: UserAnswers,
    houseConsignmentIndex: Index,
    documentIndex: Index,
    mode: Mode
  )(implicit hc: HeaderCarrier): Future[SelectableList[DocumentType]] = {
    import pages.houseConsignment.index.documents.TypePage

    val documentsF = if (mode == CheckMode) {
      userAnswers.get(TypePage(houseConsignmentIndex, documentIndex)).map(_.`type`) match {
        case Some(DocType.Transport) => referenceDataService.getTransportDocuments()
        case Some(DocType.Support)   => referenceDataService.getSupportingDocuments()
        case _                       => referenceDataService.getDocuments()
      }
    } else {
      referenceDataService.getDocuments()
    }

    documentsF.map(SelectableList(_))
  }
}
