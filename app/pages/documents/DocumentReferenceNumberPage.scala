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

import generated.{SupportingDocumentType02, TransportDocumentType01}
import models.Index
import pages.sections.documents.DocumentDetailsSection
import pages.{DiscrepancyQuestionPage, QuestionPage}
import play.api.libs.json.JsPath

case class DocumentReferenceNumberPage(documentIndex: Index) extends QuestionPage[String] {

  override def path: JsPath = DocumentDetailsSection(documentIndex).path \ toString

  override def toString: String = "referenceNumber"
}

trait BaseDocumentReferenceNumberPage[T] extends DiscrepancyQuestionPage[String, Seq[T], String] {

  val documentIndex: Index

  override def path: JsPath = DocumentReferenceNumberPage(documentIndex).path

  override def toString: String = DocumentReferenceNumberPage(documentIndex).toString
}

case class SupportingDocumentReferenceNumberPage(documentIndex: Index) extends BaseDocumentReferenceNumberPage[SupportingDocumentType02] {

  override def valueInIE043(ie043: Seq[SupportingDocumentType02], sequenceNumber: Option[BigInt]): Option[String] =
    ie043
      .find {
        x => sequenceNumber.contains(x.sequenceNumber)
      }
      .map(_.referenceNumber)
}

case class TransportDocumentReferenceNumberPage(documentIndex: Index) extends BaseDocumentReferenceNumberPage[TransportDocumentType01] {

  override def valueInIE043(ie043: Seq[TransportDocumentType01], sequenceNumber: Option[BigInt]): Option[String] =
    ie043
      .find {
        x => sequenceNumber.contains(x.sequenceNumber)
      }
      .map(_.referenceNumber)
}
