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
import models.reference.TransportMode.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.Helpers.running

class TransportModeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMode" - {

    "InlandMode" - {

      "must serialise" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val value = InlandMode(code, description)
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
              val value = InlandMode(code, description)
              Json
                .parse(s"""
                     |{
                     |  "code": "$code",
                     |  "description": "$description"
                     |}
                     |""".stripMargin)
                .as[InlandMode] mustEqual value
          }
        }

        "when reading from reference data" - {
          "when phase 5" in {
            running(_.configure("feature-flags.phase-6-enabled" -> false)) {
              app =>
                val config = app.injector.instanceOf[FrontendAppConfig]
                forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                  (code, description) =>
                    val value = InlandMode(code, description)
                    Json
                      .parse(s"""
                           |{
                           |  "code": "$code",
                           |  "description": "$description"
                           |}
                           |""".stripMargin)
                      .as[InlandMode](InlandMode.reads(config)) mustEqual value
                }
            }
          }

          "when phase 6" in {
            running(_.configure("feature-flags.phase-6-enabled" -> true)) {
              app =>
                val config = app.injector.instanceOf[FrontendAppConfig]
                forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                  (code, description) =>
                    val value = InlandMode(code, description)
                    Json
                      .parse(s"""
                           |{
                           |  "key": "$code",
                           |  "value": "$description"
                           |}
                           |""".stripMargin)
                      .as[InlandMode](InlandMode.reads(config)) mustEqual value
                }
            }
          }
        }
      }

      "must format as string" in {
        forAll(arbitrary[InlandMode]) {
          value =>
            value.toString mustEqual value.description
        }
      }

      "must order" in {
        val value1 = InlandMode("RS", "Serbia")
        val value2 = InlandMode("XS", "Serbia")
        val value3 = InlandMode("FR", "France")

        val values = NonEmptySet.of(value1, value2, value3)

        val result = values.toNonEmptyList.toList

        result mustEqual Seq(
          value3,
          value1,
          value2
        )
      }
    }

    "BorderMode" - {

      "must serialise" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val value = BorderMode(code, description)
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
              val value = BorderMode(code, description)
              Json
                .parse(s"""
                     |{
                     |  "code": "$code",
                     |  "description": "$description"
                     |}
                     |""".stripMargin)
                .as[BorderMode] mustEqual value
          }
        }

        "when reading from reference data" - {
          "when phase 5" in {
            running(_.configure("feature-flags.phase-6-enabled" -> false)) {
              app =>
                val config = app.injector.instanceOf[FrontendAppConfig]
                forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                  (code, description) =>
                    val value = BorderMode(code, description)
                    Json
                      .parse(s"""
                           |{
                           |  "code": "$code",
                           |  "description": "$description"
                           |}
                           |""".stripMargin)
                      .as[BorderMode](BorderMode.reads(config)) mustEqual value
                }
            }
          }

          "when phase 6" in {
            running(_.configure("feature-flags.phase-6-enabled" -> true)) {
              app =>
                val config = app.injector.instanceOf[FrontendAppConfig]
                forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                  (code, description) =>
                    val value = BorderMode(code, description)
                    Json
                      .parse(s"""
                           |{
                           |  "key": "$code",
                           |  "value": "$description"
                           |}
                           |""".stripMargin)
                      .as[BorderMode](BorderMode.reads(config)) mustEqual value
                }
            }
          }
        }
      }

      "must format as string" in {
        forAll(arbitrary[BorderMode]) {
          value =>
            value.toString mustEqual value.description
        }
      }

      "must order" in {
        val value1 = BorderMode("RS", "Serbia")
        val value2 = BorderMode("XS", "Serbia")
        val value3 = BorderMode("FR", "France")

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

}
