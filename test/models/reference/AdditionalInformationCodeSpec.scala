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
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

class AdditionalInformationCodeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "AdditionalInformationCode" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val value = AdditionalInformationCode(code, description)
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
            val value = AdditionalInformationCode(code, description)
            Json
              .parse(s"""
                   |{
                   |  "code": "$code",
                   |  "description": "$description"
                   |}
                   |""".stripMargin)
              .as[AdditionalInformationCode] mustEqual value
        }
      }

      "when reading from reference data" - {
        "when phase 5" in {
          running(_.configure("feature-flags.phase-6-enabled" -> false)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val value = AdditionalInformationCode(code, description)
                  Json
                    .parse(s"""
                         |{
                         |  "code": "$code",
                         |  "description": "$description"
                         |}
                         |""".stripMargin)
                    .as[AdditionalInformationCode](AdditionalInformationCode.reads(config)) mustEqual value
              }
          }
        }

        "when phase 6" in {
          running(_.configure("feature-flags.phase-6-enabled" -> true)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val value = AdditionalInformationCode(code, description)
                  Json
                    .parse(s"""
                         |{
                         |  "key": "$code",
                         |  "value": "$description"
                         |}
                         |""".stripMargin)
                    .as[AdditionalInformationCode](AdditionalInformationCode.reads(config)) mustEqual value
              }
          }
        }
      }
    }

    "must convert to select item" in {
      forAll(arbitrary[AdditionalInformationCode], arbitrary[Boolean]) {
        (value, selected) =>
          value.toSelectItem(selected) mustEqual SelectItem(
            Some(value.code),
            s"${value.code} - ${value.description}",
            selected
          )
      }
    }

    "must format as string" in {
      forAll(arbitrary[AdditionalInformationCode]) {
        value =>
          value.toString mustEqual s"${value.code} - ${value.description}"
      }
    }

    "must order" in {
      val value1 = AdditionalInformationCode("B", "Description 1")
      val value2 = AdditionalInformationCode("C", "Description 2")
      val value3 = AdditionalInformationCode("A", "Description 3")

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
