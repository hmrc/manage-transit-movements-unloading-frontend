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
import models._
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.{AdditionalReference, Country, Item, PackageType}
import org.scalacheck.{Arbitrary, Gen}
import play.api.mvc.Call
import play.api.test.Helpers.{GET, POST}

trait ModelGenerators {

  self: Generators =>

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

  implicit lazy val arbitraryTransportMeansIdentification: Arbitrary[TransportMeansIdentification] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("10", "11", "21", "30", "40", "41", "80", "81")
        description <- nonEmptyString
      } yield TransportMeansIdentification(code, description)
    }

  implicit lazy val arbitraryAdditionalReference: Arbitrary[AdditionalReference] =
    Arbitrary {
      for {
        refType     <- Gen.alphaNumStr
        description <- nonEmptyString
        refNum      <- Gen.option(nonEmptyString)
      } yield AdditionalReference(refType, description, refNum)
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
      } yield Country(code.mkString, Some(name))
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
}
