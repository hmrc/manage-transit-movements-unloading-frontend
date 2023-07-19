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

import models.reference.Country
import models.{CheckMode, EoriNumber, Mode, MovementReferenceNumber, NormalMode, UnloadingType}
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  self: Generators =>

  implicit lazy val arbitraryMovementReferenceNumber: Arbitrary[MovementReferenceNumber] =
    Arbitrary {
      for {
        year <- Gen
          .choose(0, 99)
          .map(
            y => f"$y%02d"
          )
        country <- Gen.pick(2, 'A' to 'Z')
        serial  <- Gen.pick(13, ('A' to 'Z') ++ ('0' to '9'))
      } yield MovementReferenceNumber(year, country.mkString, serial.mkString)
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
}
