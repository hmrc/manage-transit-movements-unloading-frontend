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

package models

import generated._
import models.reference.DocumentType

sealed trait Document {
  val documentType: DocumentType
  val referenceNumber: String
}

object Document {

  def apply(document: SupportingDocumentType02, documentType: DocumentType): SupportingDocument =
    SupportingDocument(
      documentType = DocumentType(
        code = documentType.code,
        description = documentType.description
      ),
      referenceNumber = document.referenceNumber,
      complementOfInformation = document.complementOfInformation
    )

  def apply(document: TransportDocumentType02, documentType: DocumentType): TransportDocument =
    TransportDocument(
      documentType = DocumentType(
        code = documentType.code,
        description = documentType.description
      ),
      referenceNumber = document.referenceNumber
    )

  case class SupportingDocument(
    documentType: DocumentType,
    referenceNumber: String,
    complementOfInformation: Option[String]
  ) extends Document

  case class TransportDocument(
    documentType: DocumentType,
    referenceNumber: String
  ) extends Document
}