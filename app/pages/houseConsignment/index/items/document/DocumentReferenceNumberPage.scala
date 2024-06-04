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

package pages.houseConsignment.index.items.document

import generated.{SupportingDocumentType02, TransportDocumentType02}
import models.Index
import pages.sections.houseConsignment.index.items.documents.DocumentSection
import pages.{DiscrepancyQuestionPage, QuestionPage}
import play.api.libs.json.JsPath

case class DocumentReferenceNumberPage(houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index) extends QuestionPage[String] {

  override def path: JsPath = DocumentSection(houseConsignmentIndex, itemIndex, documentIndex).path \ toString

  override def toString: String = "referenceNumber"
}

trait BaseDocumentReferenceNumberPage[T] extends DiscrepancyQuestionPage[String, Seq[T], String] {

  val houseConsignmentIndex: Index
  val itemIndex: Index
  val documentIndex: Index

  override def path: JsPath = DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex).path

  override def toString: String = DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex).toString
}

case class SupportingDocumentReferenceNumberPage(houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index)
    extends BaseDocumentReferenceNumberPage[SupportingDocumentType02] {

  override def valueInIE043(ie043: Seq[SupportingDocumentType02], sequenceNumber: Option[BigInt]): Option[String] =
    ie043
      .find {
        x => sequenceNumber.contains(BigInt(x.sequenceNumber))
      }
      .map(_.referenceNumber)
}

case class TransportDocumentReferenceNumberPage(houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index)
    extends BaseDocumentReferenceNumberPage[TransportDocumentType02] {

  override def valueInIE043(ie043: Seq[TransportDocumentType02], sequenceNumber: Option[BigInt]): Option[String] =
    ie043
      .find {
        x => sequenceNumber.contains(BigInt(x.sequenceNumber))
      }
      .map(_.referenceNumber)
}
