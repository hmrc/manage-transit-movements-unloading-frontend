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

import connectors.ReferenceDataConnector
import models.reference.DocumentType
import models.{CheckMode, DocType, Index, Mode, SelectableList, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DocumentsService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getDocuments()(implicit hc: HeaderCarrier): Future[SelectableList[DocumentType]] =
    for {
      transportDocuments  <- referenceDataConnector.getTransportDocuments()
      supportingDocuments <- referenceDataConnector.getSupportingDocuments()
      documents = transportDocuments ++ supportingDocuments
    } yield SelectableList(documents.toSeq)

  def getTransportDocuments()(implicit hc: HeaderCarrier): Future[SelectableList[DocumentType]] =
    referenceDataConnector
      .getTransportDocuments()
      .map(_.toSeq)
      .map(SelectableList(_))

  def getSupportingDocuments()(implicit hc: HeaderCarrier): Future[SelectableList[DocumentType]] =
    referenceDataConnector
      .getSupportingDocuments()
      .map(_.toSeq)
      .map(SelectableList(_))

  def getDocumentList(
    userAnswers: UserAnswers,
    documentIndex: Index,
    mode: Mode
  )(implicit hc: HeaderCarrier): Future[SelectableList[DocumentType]] = {
    import pages.documents.TypePage

    if (mode == CheckMode) {
      userAnswers.get(TypePage(documentIndex)).map(_.`type`) match {
        case Some(DocType.Transport) => getTransportDocuments()
        case Some(DocType.Support)   => getSupportingDocuments()
        case _                       => getDocuments()
      }
    } else {
      getDocuments()
    }
  }

  def getDocumentList(
    userAnswers: UserAnswers,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index,
    mode: Mode
  )(implicit hc: HeaderCarrier): Future[SelectableList[DocumentType]] = {
    import pages.houseConsignment.index.items.document.TypePage

    if (mode == CheckMode) {
      userAnswers.get(TypePage(houseConsignmentIndex, itemIndex, documentIndex)).map(_.`type`) match {
        case Some(DocType.Transport) => getTransportDocuments()
        case Some(DocType.Support)   => getSupportingDocuments()
        case _                       => getDocuments()
      }
    } else {
      getDocuments()
    }
  }

  def getDocumentList(
    userAnswers: UserAnswers,
    houseConsignmentIndex: Index,
    documentIndex: Index,
    mode: Mode
  )(implicit hc: HeaderCarrier): Future[SelectableList[DocumentType]] = {
    import pages.houseConsignment.index.documents.TypePage

    if (mode == CheckMode) {
      userAnswers.get(TypePage(houseConsignmentIndex, documentIndex)).map(_.`type`) match {
        case Some(DocType.Transport) => getTransportDocuments()
        case Some(DocType.Support)   => getSupportingDocuments()
        case _                       => getDocuments()
      }
    } else {
      getDocuments()
    }
  }
}
