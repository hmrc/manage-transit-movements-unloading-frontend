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
import cats.data.NonEmptySet
import config.FrontendAppConfig
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.Helpers.running

class DocTypeExciseSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "DocTypeExcise" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val value = DocTypeExcise(code, description)
          Json.toJson(value) mustEqual Json.parse(s"""
                                                     |{
                                                     |  "code": "$code",
                                                     |  "description": "$description"
                                                     |}
                                                     |""".stripMargin)
      }
    }

    "must deserialise" - {
      "when reading from mongo" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val value = DocTypeExcise(code, description)
            Json
              .parse(s"""
                        |{
                        |  "code": "$code",
                        |  "description": "$description"
                        |}
                        |""".stripMargin)
              .as[DocTypeExcise] mustEqual value
        }
      }

      "when reading from reference data" - {
        "when phase 5" in {
          running(_.configure("feature-flags.phase-6-enabled" -> false)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val value = DocTypeExcise(code, description)
                  Json
                    .parse(s"""
                              |{
                              |  "code": "$code",
                              |  "description": "$description"
                              |}
                              |""".stripMargin)
                    .as[DocTypeExcise](DocTypeExcise.reads(config)) mustEqual value
              }
          }
        }

        "when phase 6" in {
          running(_.configure("feature-flags.phase-6-enabled" -> true)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val value = DocTypeExcise(code, description)
                  Json
                    .parse(s"""
                              |{
                              |  "key": "$code",
                              |  "value": "$description"
                              |}
                              |""".stripMargin)
                    .as[DocTypeExcise](DocTypeExcise.reads(config)) mustEqual value
              }
          }
        }
      }
    }

    "must order" in {
      val value1 = DocTypeExcise("RS", "Serbia")
      val value2 = DocTypeExcise("XS", "Serbia")
      val value3 = DocTypeExcise("FR", "France")

      val values = NonEmptySet.of(value1, value2, value3)

      val result = values.toNonEmptyList.toList

      result mustEqual Seq(
        value3,
        value1,
        value2
      )
    }
  }
}
