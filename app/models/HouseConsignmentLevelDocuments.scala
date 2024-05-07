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

package models

import config.FrontendAppConfig
import models.reference.DocumentType
import pages.houseConsignment.index.items.document.TypePage
import pages.houseConsignment.index.documents.{TypePage => HouseTypePage}
import pages.sections.houseConsignment.index.items.documents.DocumentsSection
import pages.sections.houseConsignment.index.documents.{DocumentsSection => HouseDocumentSection}

case class HouseConsignmentLevelDocuments(
  supporting: Int,
  transport: Int
) {

  def canAdd(documentType: DocType)(implicit config: FrontendAppConfig): Boolean = documentType match {
    case DocType.Support   => supporting < config.maxSupportingDocumentsHouseConsignment
    case DocType.Transport => transport < config.maxTransportDocumentsHouseConsignment
    case DocType.Previous  => false
  }

  def canAddMore(implicit config: FrontendAppConfig): Boolean =
    canAdd(DocType.Support) || canAdd(DocType.Transport)

  def availableDocuments(documents: Seq[DocumentType])(implicit config: FrontendAppConfig): Seq[DocumentType] =
    documents
      .filter(
        doc => canAdd(doc.`type`)
      )
}

object HouseConsignmentLevelDocuments {

  def apply(): HouseConsignmentLevelDocuments = HouseConsignmentLevelDocuments(0, 0)

  private def apply(values: (Int, Int)): HouseConsignmentLevelDocuments = HouseConsignmentLevelDocuments(values._1, values._2)

  def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index): HouseConsignmentLevelDocuments =
    HouseConsignmentLevelDocuments(userAnswers, houseConsignmentIndex, itemIndex, Some(documentIndex))

  def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Option[Index]): HouseConsignmentLevelDocuments = {
    val numberOfDocuments = userAnswers.get(DocumentsSection(houseConsignmentIndex, itemIndex)).map(_.value.size).getOrElse(0)

    (0 until numberOfDocuments).map(Index(_)).foldLeft(HouseConsignmentLevelDocuments()) {
      case (HouseConsignmentLevelDocuments(supporting, transport), index) if !documentIndex.contains(index) =>
        val values = userAnswers.get(TypePage(houseConsignmentIndex, itemIndex, index)).map(_.`type`) match {
          case Some(DocType.Support)   => (supporting + 1, transport)
          case Some(DocType.Transport) => (supporting, transport + 1)
          case _                       => (supporting, transport)
        }
        HouseConsignmentLevelDocuments(values)
      case (houseConsignmentLevelDocuments, _) => houseConsignmentLevelDocuments
    }
  }

  def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index, documentIndex: Option[Index]): HouseConsignmentLevelDocuments = {
    val numberOfDocuments = userAnswers.get(HouseDocumentSection(houseConsignmentIndex)).map(_.value.size).getOrElse(0)

    (0 until numberOfDocuments).map(Index(_)).foldLeft(HouseConsignmentLevelDocuments()) {
      case (HouseConsignmentLevelDocuments(supporting, transport), index) if !documentIndex.contains(index) =>
        val values = userAnswers.get(HouseTypePage(houseConsignmentIndex, index)).map(_.`type`) match {
          case Some(DocType.Support)   => (supporting + 1, transport)
          case Some(DocType.Transport) => (supporting, transport + 1)
          case _                       => (supporting, transport)
        }
        HouseConsignmentLevelDocuments(values)
      case (houseConsignmentLevelDocuments, _) => houseConsignmentLevelDocuments
    }
  }

  def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index, documentIndex: Index): HouseConsignmentLevelDocuments = {

    import pages.houseConsignment.index.documents.TypePage
    import pages.sections.houseConsignment.index.documents.DocumentsSection

    val numberOfDocuments = userAnswers.get(DocumentsSection(houseConsignmentIndex)).map(_.value.size).getOrElse(0)

    (0 until numberOfDocuments).map(Index(_)).foldLeft(HouseConsignmentLevelDocuments()) {
      case (HouseConsignmentLevelDocuments(supporting, transport), index) if documentIndex != index =>
        val values = userAnswers.get(TypePage(houseConsignmentIndex, index)).map(_.`type`) match {
          case Some(DocType.Support)   => (supporting + 1, transport)
          case Some(DocType.Transport) => (supporting, transport + 1)
          case _                       => (supporting, transport)
        }
        HouseConsignmentLevelDocuments(values)
      case (houseConsignmentLevelDocuments, _) => houseConsignmentLevelDocuments
    }
  }
}
