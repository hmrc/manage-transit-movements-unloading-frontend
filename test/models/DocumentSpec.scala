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

import base.SpecBase
import generated._
import models.DocType.{Previous, Support, Transport}
import models.Document._
import models.reference.DocumentType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class DocumentSpec extends SpecBase with ScalaCheckPropertyChecks {

  "apply" - {
    "must convert SupportingDocumentType02 to SupportingDocument" in {
      forAll(arbitrary[BigInt], Gen.alphaNumStr, Gen.alphaNumStr, Gen.option(Gen.alphaNumStr), Gen.alphaNumStr) {
        (sequenceNumber, typeValue, referenceNumber, complementOfInformation, description) =>
          val ie043Document = SupportingDocumentType02(
            sequenceNumber = sequenceNumber,
            typeValue = typeValue,
            referenceNumber = referenceNumber,
            complementOfInformation = complementOfInformation
          )

          val documentType = DocumentType(
            `type` = Support,
            code = typeValue,
            description = description
          )

          val result = Document.apply(ie043Document, documentType)

          result mustBe SupportingDocument(
            sequenceNumber = sequenceNumber,
            documentType = documentType,
            referenceNumber = referenceNumber,
            complementOfInformation = complementOfInformation
          )
      }
    }

    "must convert TransportDocumentType02 to TransportDocument" in {
      forAll(arbitrary[BigInt], Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
        (sequenceNumber, typeValue, referenceNumber, description) =>
          val ie043Document = TransportDocumentType02(
            sequenceNumber = sequenceNumber,
            typeValue = typeValue,
            referenceNumber = referenceNumber
          )

          val documentType = DocumentType(
            `type` = Transport,
            code = typeValue,
            description = description
          )

          val result = Document.apply(ie043Document, documentType)

          result mustBe TransportDocument(
            sequenceNumber = sequenceNumber,
            documentType = documentType,
            referenceNumber = referenceNumber
          )
      }
    }

    "must convert PreviousDocumentType06 to PreviousDocumentType" in {
      forAll(arbitrary[BigInt], Gen.alphaNumStr, Gen.alphaNumStr, Gen.option(Gen.alphaNumStr), Gen.alphaNumStr) {
        (sequenceNumber, typeValue, referenceNumber, complementOfInformation, description) =>
          val ie043Document = PreviousDocumentType06(
            sequenceNumber = sequenceNumber,
            typeValue = typeValue,
            referenceNumber = referenceNumber,
            complementOfInformation = complementOfInformation
          )

          val documentType = DocumentType(
            `type` = Previous,
            code = typeValue,
            description = description
          )

          val result = Document.apply(ie043Document, documentType)

          result mustBe PreviousDocument(
            sequenceNumber = sequenceNumber,
            documentType = documentType,
            referenceNumber = referenceNumber,
            complementOfInformation = complementOfInformation
          )
      }
    }

    "must convert PreviousDocumentType04 to PreviousDocumentType" in {
      forAll(arbitrary[BigInt], Gen.alphaNumStr, Gen.alphaNumStr, Gen.option(arbitrary[BigInt]), Gen.option(Gen.alphaNumStr), Gen.alphaNumStr) {
        (sequenceNumber, typeValue, referenceNumber, goodsItemNumber, complementOfInformation, description) =>
          val ie043Document = PreviousDocumentType04(
            sequenceNumber = sequenceNumber,
            typeValue = typeValue,
            referenceNumber = referenceNumber,
            goodsItemNumber = goodsItemNumber,
            complementOfInformation = complementOfInformation
          )

          val documentType = DocumentType(
            `type` = Previous,
            code = typeValue,
            description = description
          )

          val result = Document.apply(ie043Document, documentType)

          result mustBe PreviousDocument(
            sequenceNumber = sequenceNumber,
            documentType = documentType,
            referenceNumber = referenceNumber,
            complementOfInformation = complementOfInformation
          )
      }
    }
  }
}
