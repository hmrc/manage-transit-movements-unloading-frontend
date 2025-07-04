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

import generated.*
import models.DocType.*

sealed trait Document {
  val `type`: DocType
  val sequenceNumber: BigInt
  val typeValue: String
  val referenceNumber: String
}

object Document {

  case class SupportingDocument(
    sequenceNumber: BigInt,
    typeValue: String,
    referenceNumber: String,
    complementOfInformation: Option[String]
  ) extends Document {

    override val `type`: DocType = Support
  }

  object SupportingDocument {

    def apply(document: SupportingDocumentType02): SupportingDocument =
      new SupportingDocument(
        sequenceNumber = document.sequenceNumber,
        typeValue = document.typeValue,
        referenceNumber = document.referenceNumber,
        complementOfInformation = document.complementOfInformation
      )
  }

  case class TransportDocument(
    sequenceNumber: BigInt,
    typeValue: String,
    referenceNumber: String
  ) extends Document {

    override val `type`: DocType = Transport
  }

  object TransportDocument {

    def apply(document: TransportDocumentType01): TransportDocument =
      new TransportDocument(
        sequenceNumber = document.sequenceNumber,
        typeValue = document.typeValue,
        referenceNumber = document.referenceNumber
      )
  }

  case class PreviousDocument(
    sequenceNumber: BigInt,
    typeValue: String,
    referenceNumber: String,
    complementOfInformation: Option[String]
  ) extends Document {

    override val `type`: DocType = Previous
  }

  object PreviousDocument {

    def apply(document: PreviousDocumentType03): PreviousDocument =
      new PreviousDocument(
        sequenceNumber = document.sequenceNumber,
        typeValue = document.typeValue,
        referenceNumber = document.referenceNumber,
        complementOfInformation = document.complementOfInformation
      )

    def apply(document: PreviousDocumentType05): PreviousDocument =
      new PreviousDocument(
        sequenceNumber = document.sequenceNumber,
        typeValue = document.typeValue,
        referenceNumber = document.referenceNumber,
        complementOfInformation = document.complementOfInformation
      )

    def apply(document: PreviousDocumentType06): PreviousDocument =
      new PreviousDocument(
        sequenceNumber = document.sequenceNumber,
        typeValue = document.typeValue,
        referenceNumber = document.referenceNumber,
        complementOfInformation = document.complementOfInformation
      )
  }
}
