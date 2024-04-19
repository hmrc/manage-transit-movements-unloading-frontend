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

package pages.documents

import generated.{SupportingDocumentType02, TransportDocumentType02}
import models.Index
import models.reference.DocumentType
import pages.sections.documents.DocumentSection
import pages.{DiscrepancyQuestionPage, QuestionPage}
import play.api.libs.json.JsPath

case class TypePage(documentIndex: Index) extends QuestionPage[DocumentType] {

  override def path: JsPath = DocumentSection(documentIndex).path \ toString

  override def toString: String = "type"
}

case class SupportingTypePage(documentIndex: Index) extends DiscrepancyQuestionPage[DocumentType, Seq[SupportingDocumentType02], String] {

  override def path: JsPath = TypePage(documentIndex).path

  override def toString: String = TypePage(documentIndex).toString

  override def valueInIE043(ie043: Seq[SupportingDocumentType02], sequenceNumber: Option[BigInt]): Option[String] =
    ie043
      .find {
        x => sequenceNumber.contains(x.sequenceNumber)
      }
      .map(_.typeValue)
}

case class TransportTypePage(documentIndex: Index) extends DiscrepancyQuestionPage[DocumentType, Seq[TransportDocumentType02], String] {

  override def path: JsPath = TypePage(documentIndex).path

  override def toString: String = TypePage(documentIndex).toString

  override def valueInIE043(ie043: Seq[TransportDocumentType02], sequenceNumber: Option[BigInt]): Option[String] =
    ie043
      .find {
        x => sequenceNumber.contains(x.sequenceNumber)
      }
      .map(_.typeValue)
}
