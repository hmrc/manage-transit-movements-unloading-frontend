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
import pages.documents.TypePage
import pages.sections.documents.DocumentsSection

case class ConsignmentLevelDocuments(
  supporting: Int,
  transport: Int
) {

  def canAdd(documentType: DocType)(implicit config: FrontendAppConfig): Boolean = documentType match {
    case DocType.Support   => supporting < config.maxSupportingDocuments
    case DocType.Transport => transport < config.maxTransportDocuments
    case DocType.Previous  => false
  }

  def canAddMore(implicit config: FrontendAppConfig): Boolean =
    canAdd(DocType.Support) || canAdd(DocType.Transport)

  def availableDocuments(documents: Seq[DocumentType])(implicit config: FrontendAppConfig) =
    documents
      .filter(
        doc => canAdd(doc.`type`)
      )
}

object ConsignmentLevelDocuments {

  def apply(): ConsignmentLevelDocuments = ConsignmentLevelDocuments(0, 0)

  private def apply(values: (Int, Int)): ConsignmentLevelDocuments = ConsignmentLevelDocuments(values._1, values._2)

  def apply(userAnswers: UserAnswers, documentIndex: Index): ConsignmentLevelDocuments =
    ConsignmentLevelDocuments(userAnswers, Some(documentIndex))

  def apply(userAnswers: UserAnswers, documentIndex: Option[Index] = None): ConsignmentLevelDocuments = {
    val numberOfDocuments = userAnswers.get(DocumentsSection).map(_.value.size).getOrElse(0)

    (0 until numberOfDocuments).map(Index(_)).foldLeft(ConsignmentLevelDocuments()) {
      case (ConsignmentLevelDocuments(supporting, transport), index) if !documentIndex.contains(index) =>
        val values = userAnswers.get(TypePage(index)).map(_.`type`) match {
          case Some(DocType.Support)   => (supporting + 1, transport)
          case Some(DocType.Transport) => (supporting, transport + 1)
          case _                       => (supporting, transport)
        }
        ConsignmentLevelDocuments(values)
      case (consignmentLevelDocuments, _) => consignmentLevelDocuments
    }
  }
}
