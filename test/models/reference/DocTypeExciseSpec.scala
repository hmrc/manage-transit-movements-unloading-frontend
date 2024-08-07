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

package models.reference

import base.SpecBase
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class DocTypeExciseSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Country" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
        (activeFrom, code, state, description) =>
          val docTypeExcise = DocTypeExcise(activeFrom, code, state, description)
          Json.toJson(docTypeExcise) mustBe Json.parse(s"""
              |{
              |  "activeFrom": "$activeFrom",
              |  "code": "$code",
              |  "state": "$state",
              |  "description": "$description"
              |}
              |""".stripMargin)
      }
    }

    "must deserialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
        (activeFrom, code, state, description) =>
          val docTypeExcise = DocTypeExcise(activeFrom, code, state, description)
          Json
            .parse(s"""
              |{
              |  "activeFrom": "$activeFrom",
              |  "code": "$code",
              |  "state": "$state",
              |  "description": "$description"
              |}
              |""".stripMargin)
            .as[DocTypeExcise] mustBe docTypeExcise
      }
    }
  }
}
