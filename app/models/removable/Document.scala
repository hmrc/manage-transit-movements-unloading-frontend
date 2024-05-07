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

package models.removable

import models.reference.DocumentType
import models.{Index, UserAnswers}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads

case class Document(`type`: DocumentType, referenceNumber: Option[String]) {

  def forDisplay: String = referenceNumber match {
    case Some(rn) => s"${`type`.toString} - $rn"
    case None     => `type`.toString
  }
}

object Document {

  def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index): Option[Document] = {
    import pages.houseConsignment.index.items.document._
    implicit val reads: Reads[Document] = (
      TypePage(houseConsignmentIndex, itemIndex, documentIndex).path.read[DocumentType] and
        DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex).path.readNullable[String]
    ).apply {
      (documentType, referenceNumber) => Document(documentType, referenceNumber)
    }
    userAnswers.data.asOpt[Document]
  }

  def apply(userAnswers: UserAnswers, houseConsignmentIndex: Index, documentIndex: Index): Option[Document] = {
    import pages.houseConsignment.index.documents._
    implicit val reads: Reads[Document] = (
      TypePage(houseConsignmentIndex, documentIndex).path.read[DocumentType] and
        DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex).path.readNullable[String]
    ).apply {
      (documentType, referenceNumber) => Document(documentType, referenceNumber)
    }
    userAnswers.data.asOpt[Document]
  }

  def apply(userAnswers: UserAnswers, documentIndex: Index): Option[Document] = {
    import pages.documents._
    implicit val reads: Reads[Document] = (
      TypePage(documentIndex).path.read[DocumentType] and
        DocumentReferenceNumberPage(documentIndex).path.readNullable[String]
    ).apply {
      (documentType, referenceNumber) => Document(documentType, referenceNumber)
    }
    userAnswers.data.asOpt[Document]
  }

}
