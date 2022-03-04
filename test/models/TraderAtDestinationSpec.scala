/*
 * Copyright 2022 HM Revenue & Customs
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

import generators.MessagesModelGenerators
import models.XMLWrites._
import models.messages.escapeXml
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.StreamlinedXmlEquality
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TraderAtDestinationSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality {

  "TraderDestination" - {

    "must serialize" in {
      forAll(arbitrary[TraderAtDestination]) {
        trader =>
          val expectedResult =
            <TRADESTRD>
              <NamTRD7>{escapeXml(trader.name)}</NamTRD7>
              <StrAndNumTRD22>{trader.streetAndNumber}</StrAndNumTRD22>
              <PosCodTRD23>{trader.postCode}</PosCodTRD23>
              <CitTRD24>{trader.city}</CitTRD24>
              <CouTRD25>{trader.countryCode}</CouTRD25>
              <NADLNGRD>EN</NADLNGRD>
              <TINTRD59>{trader.eori}</TINTRD59>
            </TRADESTRD>

          trader.toXml mustEqual expectedResult
      }
    }
  }

}
