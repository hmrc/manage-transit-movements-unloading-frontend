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

package models

import com.lucidchart.open.xtract.XmlReader
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsString, Json}

import scala.xml.Node

class MovementReferenceNumberSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with EitherValues with OptionValues {

  "a Movement Reference Number" - {

    "must deserialise" in {

      forAll(arbitrary[MovementReferenceNumber]) {
        mrn =>
          JsString(mrn.toString).as[MovementReferenceNumber] mustEqual mrn
      }
    }

    "must serialise" in {

      forAll(arbitrary[MovementReferenceNumber]) {
        mrn =>
          Json.toJson(mrn) mustEqual JsString(mrn.toString)
      }
    }

    "XML" - {
      "must read xml as MovementReferenceNumber" in {
        forAll(arbitrary[MovementReferenceNumber]) {
          mrn =>
            val xml: Node = <HEAHEA>{<DocNumHEA5>{mrn}</DocNumHEA5>}</HEAHEA>
            val result    = XmlReader.of[MovementReferenceNumber].read(xml)
            result.toOption.value mustEqual mrn
        }
      }
    }
  }
}
