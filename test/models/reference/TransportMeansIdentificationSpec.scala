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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.Helpers.running

class TransportMeansIdentificationSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansIdentification" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val value = TransportMeansIdentification(code, description)
          Json.toJson(value) mustEqual Json.parse(s"""
              |{
              |  "type": "$code",
              |  "description": "$description"
              |}
              |""".stripMargin)
      }
    }

    "must deserialise" - {
      "when reading from mongo" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val value = TransportMeansIdentification(code, description)
            Json
              .parse(s"""
                   |{
                   |  "type": "$code",
                   |  "description": "$description"
                   |}
                   |""".stripMargin)
              .as[TransportMeansIdentification] mustEqual value
        }
      }

      "when reading from reference data" - {
        "when phase 5" in {
          running(_.configure("feature-flags.phase-6-enabled" -> false)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val value = TransportMeansIdentification(code, description)
                  Json
                    .parse(s"""
                         |{
                         |  "type": "$code",
                         |  "description": "$description"
                         |}
                         |""".stripMargin)
                    .as[TransportMeansIdentification](TransportMeansIdentification.reads(config)) mustEqual value
              }
          }
        }

        "when phase 6" in {
          running(_.configure("feature-flags.phase-6-enabled" -> true)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val value = TransportMeansIdentification(code, description)
                  Json
                    .parse(s"""
                         |{
                         |  "key": "$code",
                         |  "value": "$description"
                         |}
                         |""".stripMargin)
                    .as[TransportMeansIdentification](TransportMeansIdentification.reads(config)) mustEqual value
              }
          }
        }
      }
    }

    "must format as string" in {
      forAll(arbitrary[TransportMeansIdentification]) {
        value =>
          value.toString mustEqual value.description
      }
    }

    "must order" in {
      val value1 = TransportMeansIdentification("RS", "Serbia")
      val value2 = TransportMeansIdentification("XS", "Serbia")
      val value3 = TransportMeansIdentification("FR", "France")

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
