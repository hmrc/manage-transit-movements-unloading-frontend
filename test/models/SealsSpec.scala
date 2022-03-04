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

import com.lucidchart.open.xtract.{ParseSuccess, XmlReader}
import generators.Generators
import models.XMLWrites._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.StreamlinedXmlEquality
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.Elem
import scala.xml.Utility.trim

class SealsSpec extends AnyFreeSpec with Matchers with Generators with ScalaCheckPropertyChecks with StreamlinedXmlEquality {

  "SealsSpec" - {

    "must serialise seals from Xml" in {

      forAll(arbitrary[Seals]) {

        seals =>
          val sealsNodes: Seq[Elem] = seals.SealId.map {
            sealId =>
              <SEAIDSID>
                <SeaIdeSID1>{sealId}</SeaIdeSID1>
                <SeaIdeSID1LNG>EN</SeaIdeSID1LNG>
              </SEAIDSID>
          }

          val expectedResult =
            <SEAINFSLI>
              <SeaNumSLI2>{seals.numberOfSeals}</SeaNumSLI2>
              {sealsNodes}
            </SEAINFSLI>

          XmlReader.of[Seals].read(trim(expectedResult)) mustBe
            ParseSuccess(Seals(seals.numberOfSeals, seals.SealId))
      }

    }

    "must serialise seals to Xml" in {

      forAll(arbitrary[Seals]) {

        seals =>
          val sealsNodes: Seq[Elem] = seals.SealId.map {
            sealId =>
              <SEAIDSID>
                <SeaIdeSID1>{sealId}</SeaIdeSID1>
                <SeaIdeSID1LNG>EN</SeaIdeSID1LNG>
              </SEAIDSID>
          }

          val expectedResult =
            <SEAINFSLI>
              <SeaNumSLI2>{seals.numberOfSeals}</SeaNumSLI2>
              {sealsNodes}
            </SEAINFSLI>

          seals.toXml mustEqual expectedResult
      }
    }

  }

}
