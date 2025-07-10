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
import generated.*
import generators.Generators
import models.DocType.{Previous, Support, Transport}
import models.Document.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class DocumentSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "apply" - {
    "must convert SupportingDocumentType02 to SupportingDocument" in {
      forAll(arbitrary[SupportingDocumentType02]) {
        document =>
          val result = SupportingDocument(document)

          result mustEqual SupportingDocument(
            sequenceNumber = document.sequenceNumber,
            typeValue = document.typeValue,
            referenceNumber = document.referenceNumber,
            complementOfInformation = document.complementOfInformation
          )

          result.`type` mustEqual Support
      }
    }

    "must convert TransportDocumentType01 to TransportDocument" in {
      forAll(arbitrary[TransportDocumentType01]) {
        document =>
          val result = TransportDocument(document)

          result mustEqual TransportDocument(
            sequenceNumber = document.sequenceNumber,
            typeValue = document.typeValue,
            referenceNumber = document.referenceNumber
          )

          result.`type` mustEqual Transport
      }
    }

    "must convert PreviousDocumentType03 to PreviousDocument" in {
      forAll(arbitrary[PreviousDocumentType03]) {
        document =>
          val result = PreviousDocument(document)

          result mustEqual PreviousDocument(
            sequenceNumber = document.sequenceNumber,
            typeValue = document.typeValue,
            referenceNumber = document.referenceNumber,
            complementOfInformation = document.complementOfInformation
          )

          result.`type` mustEqual Previous
      }
    }

    "must convert PreviousDocumentType05 to PreviousDocument" in {
      forAll(arbitrary[PreviousDocumentType05]) {
        document =>
          val result = PreviousDocument(document)

          result mustEqual PreviousDocument(
            sequenceNumber = document.sequenceNumber,
            typeValue = document.typeValue,
            referenceNumber = document.referenceNumber,
            complementOfInformation = document.complementOfInformation
          )

          result.`type` mustEqual Previous
      }
    }

    "must convert PreviousDocumentType06 to PreviousDocument" in {
      forAll(arbitrary[PreviousDocumentType06]) {
        document =>
          val result = PreviousDocument(document)

          result mustEqual PreviousDocument(
            sequenceNumber = document.sequenceNumber,
            typeValue = document.typeValue,
            referenceNumber = document.referenceNumber,
            complementOfInformation = document.complementOfInformation
          )

          result.`type` mustEqual Previous
      }
    }
  }
}
