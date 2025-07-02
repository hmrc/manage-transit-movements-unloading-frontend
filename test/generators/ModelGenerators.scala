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

package generators

import models.*
import models.P5.ArrivalMessageType
import models.reference.*
import models.reference.TransportMode.{BorderMode, InlandMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.mvc.Call
import play.api.test.Helpers.{GET, POST}

trait ModelGenerators {

  self: Generators =>

  implicit def arbitrarySelectableList[T <: Selectable](implicit arbitrary: Arbitrary[T]): Arbitrary[SelectableList[T]] = Arbitrary {
    for {
      values <- listWithMaxLength[T]()
    } yield SelectableList(values.distinctBy(_.value))
  }

  implicit lazy val arbitraryCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- Gen.alphaNumStr
        name        <- Gen.alphaNumStr
        countryId   <- Gen.alphaNumStr
        phoneNumber <- Gen.numStr
      } yield CustomsOffice(id, name, countryId, Some(phoneNumber))
    }

  implicit lazy val arbitraryIndex: Arbitrary[Index] =
    Arbitrary {
      for {
        position <- positiveInts
      } yield Index(position)
    }

  implicit lazy val arbitraryMovementReferenceNumber: Arbitrary[MovementReferenceNumber] =
    Arbitrary {
      for {
        mrn <- Gen.alphaNumStr
      } yield MovementReferenceNumber(mrn)
    }

  implicit lazy val arbitraryArrivalId: Arbitrary[ArrivalId] =
    Arbitrary {
      for {
        value <- Gen.alphaNumStr
      } yield ArrivalId(value)
    }

  implicit lazy val arbitrarySecurityDetailsType: Arbitrary[SecurityType] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("0", "1", "2", "3")
        description <- nonEmptyString
      } yield SecurityType(code, description)
    }

  implicit lazy val arbitraryTransportMeansIdentification: Arbitrary[TransportMeansIdentification] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("10", "11", "21", "30", "40", "41", "80", "81")
        description <- nonEmptyString
      } yield TransportMeansIdentification(code, description)
    }

  implicit lazy val arbitraryAddress: Arbitrary[DynamicAddress] =
    Arbitrary {
      for {
        streetAndNumber <- Gen.oneOf("10", "11", "21", "30", "40", "41", "80", "81")
        city            <- nonEmptyString
        postCode        <- Gen.option(Gen.alphaNumStr)
      } yield DynamicAddress(streetAndNumber, city, postCode)
    }

  implicit lazy val arbitraryAdditionalReference: Arbitrary[AdditionalReferenceType] =
    Arbitrary {
      for {
        refType     <- nonEmptyString
        description <- nonEmptyString
      } yield AdditionalReferenceType(refType, description)
    }

  implicit lazy val arbitraryCusCode: Arbitrary[CUSCode] =
    Arbitrary {
      for {
        code <- nonEmptyString
      } yield CUSCode(code)
    }

  implicit lazy val arbitraryDocTypeExcise: Arbitrary[DocTypeExcise] =
    Arbitrary {
      for {
        code        <- nonEmptyString
        description <- nonEmptyString
      } yield DocTypeExcise(code, description)
    }

  implicit lazy val arbitraryAdditionalInformationCode: Arbitrary[AdditionalInformationCode] =
    Arbitrary {
      for {
        code        <- Gen.alphaNumStr
        description <- nonEmptyString
      } yield AdditionalInformationCode(code, description)
    }

  implicit lazy val arbitraryEoriNumber: Arbitrary[EoriNumber] =
    Arbitrary {
      for {
        number <- stringsWithMaxLength(EoriNumber.eoriLength)
      } yield EoriNumber(number)
    }

  implicit lazy val arbitraryUnloadingType: Arbitrary[UnloadingType] =
    Arbitrary {
      Gen.oneOf(UnloadingType.values)
    }

  implicit lazy val arbitraryArrivalMessageType: Arbitrary[ArrivalMessageType] = {
    import models.P5.ArrivalMessageType.*
    Arbitrary {
      for {
        value <- nonEmptyString
        result <- Gen.oneOf(
          ArrivalNotification,
          GoodsReleasedNotification,
          UnloadingPermission,
          UnloadingRemarks,
          RejectionFromOfficeOfDestination,
          Other(value)
        )
      } yield result
    }
  }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        code <- Gen.pick(2, 'A' to 'Z')
        name <- nonEmptyString
      } yield Country(code.mkString, name)
    }

  implicit lazy val arbitraryMode: Arbitrary[Mode] =
    Arbitrary {
      Gen.oneOf(NormalMode, CheckMode)
    }

  implicit lazy val arbitraryPackageType: Arbitrary[PackageType] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield PackageType(code, desc)
    }

  implicit lazy val arbitraryIncident: Arbitrary[Incident] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield Incident(code, desc)
    }

  implicit lazy val arbitraryQualifierOfIdentification: Arbitrary[QualifierOfIdentification] =
    Arbitrary {
      for {
        qualifier   <- nonEmptyString
        description <- nonEmptyString
      } yield QualifierOfIdentification(qualifier, description)
    }

  implicit lazy val arbitraryGoodsReference: Arbitrary[GoodsReference] =
    Arbitrary {
      for {
        declarationGoodsItemNumber <- positiveInts
        itemDescription            <- nonEmptyString
      } yield GoodsReference(declarationGoodsItemNumber, itemDescription)
    }

  implicit lazy val arbitraryCall: Arbitrary[Call] = Arbitrary {
    for {
      method <- Gen.oneOf(GET, POST)
      url    <- nonEmptyString
    } yield Call(method, url)
  }

  implicit lazy val arbitraryDocumentType: Arbitrary[DocumentType] =
    Arbitrary {
      for {
        docType <- arbitrary[DocType]
        code    <- nonEmptyString
        desc    <- nonEmptyString
      } yield DocumentType(docType, code, desc)
    }

  lazy val arbitraryTransportDocument: Arbitrary[DocumentType] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield DocumentType(DocType.Transport, code, desc)
    }

  lazy val arbitrarySupportDocument: Arbitrary[DocumentType] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield DocumentType(DocType.Support, code, desc)
    }

  lazy val arbitraryPreviousDocument: Arbitrary[DocumentType] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield DocumentType(DocType.Previous, code, desc)
    }

  lazy val arbitraryTransportOrSupportDocument: Arbitrary[DocumentType] =
    Arbitrary {
      for {
        docType <- Gen.oneOf(DocType.Transport, DocType.Support)
        code    <- nonEmptyString
        desc    <- nonEmptyString
      } yield DocumentType(docType, code, desc)
    }

  implicit lazy val arbitraryDocType: Arbitrary[DocType] =
    Arbitrary {
      Gen.oneOf(DocType.values)
    }

  implicit lazy val arbitraryCoordinates: Arbitrary[Coordinates] = Arbitrary {
    for {
      long <- Gen.alphaNumStr
      lat  <- Gen.alphaNumStr
    } yield Coordinates(long, lat)
  }

  implicit lazy val arbitraryInlandMode: Arbitrary[InlandMode] =
    Arbitrary {
      Gen.oneOf(InlandMode("1", "Maritime Transport"), InlandMode("2", "Rail Transport"))
    }

  implicit lazy val arbitraryBorderMode: Arbitrary[BorderMode] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield BorderMode(code, desc)
    }

  implicit lazy val arbitraryProcedure: Arbitrary[Procedure] =
    Arbitrary {
      Gen.oneOf(
        Procedure.Unrevised,
        Procedure.CannotUseRevisedDueToConditions,
        Procedure.CannotUseRevisedDueToDiscrepancies,
        Procedure.RevisedAndGoodsTooLarge,
        Procedure.RevisedAndGoodsNotTooLarge
      )
    }
}
