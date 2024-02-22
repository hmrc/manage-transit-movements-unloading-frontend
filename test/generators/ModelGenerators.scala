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

import models.P5.ArrivalMessageType
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.{AdditionalReferenceType, Country, CustomsOffice, Item, PackageType}
import models.reference._
import models.reference.{AdditionalReferenceType, Country, Item, PackageType}
import models.{DeclarationType, SecurityType, _}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import play.api.mvc.Call
import play.api.test.Helpers.{GET, POST}

trait ModelGenerators {

  self: Generators =>

  implicit lazy val arbitraryCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- Gen.alphaNumStr
        name        <- Gen.alphaNumStr
        countryId   <- Gen.alphaNumStr
        phoneNumber <- Gen.numStr
      } yield CustomsOffice(id, name, countryId, Some(phoneNumber))
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

  implicit lazy val arbitraryAdditionalReference: Arbitrary[AdditionalReferenceType] =
    Arbitrary {
      for {
        refType     <- nonEmptyString
        description <- nonEmptyString
      } yield AdditionalReferenceType(refType, description)
    }

  implicit lazy val arbitraryDeclarationType: Arbitrary[DeclarationType] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("T", "T1", "T2", "T2F", "TIR")
        description <- nonEmptyString
      } yield DeclarationType(code, description)
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

  implicit lazy val arbitraryArrivalMessageType: Arbitrary[ArrivalMessageType] =
    Arbitrary {
      Gen.oneOf(ArrivalMessageType.values)
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
        desc <- Gen.option(nonEmptyString)
      } yield PackageType(code, desc)
    }

  implicit lazy val arbitraryItem: Arbitrary[Item] =
    Arbitrary {
      for {
        no   <- positiveInts
        desc <- nonEmptyString
      } yield Item(no, desc)
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

  implicit lazy val arbitraryDocType: Arbitrary[DocType] =
    Arbitrary {
      Gen.oneOf(DocType.values)
    }
}
