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
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

class CountrySpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  "Country" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val value = Country(code, description)
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
            val value = Country(code, description)
            Json
              .parse(s"""
                   |{
                   |  "code": "$code",
                   |  "description": "$description"
                   |}
                   |""".stripMargin)
              .as[Country] mustEqual value
        }
      }

      "when reading from reference data" - {
        "when phase 5" in {
          when(mockFrontendAppConfig.phase6Enabled).thenReturn(false)
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (code, description) =>
              val value = Country(code, description)
              Json
                .parse(s"""
                         |{
                         |  "code": "$code",
                         |  "description": "$description"
                         |}
                         |""".stripMargin)
                .as[Country](Country.reads(mockFrontendAppConfig)) mustEqual value
          }
        }

        "when phase 6" in {
          when(mockFrontendAppConfig.phase6Enabled).thenReturn(true)
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (code, description) =>
              val value = Country(code, description)
              Json
                .parse(s"""
                     |{
                     |  "key": "$code",
                     |  "value": "$description"
                     |}
                     |""".stripMargin)
                .as[Country](Country.reads(mockFrontendAppConfig)) mustEqual value
          }
        }

      }
    }

    "must convert to select item" in {
      forAll(arbitrary[Country], arbitrary[Boolean]) {
        (value, selected) =>
          value.toSelectItem(selected) mustEqual SelectItem(Some(value.code), s"${value.description} - ${value.code}", selected)
      }
    }

    "must format as string" in {
      forAll(arbitrary[Country]) {
        value =>
          value.toString mustEqual s"${value.description} - ${value.code}"
      }
    }

    "must order" in {
      val value1 = Country("RS", "Serbia")
      val value2 = Country("XS", "Serbia")
      val value3 = Country("FR", "France")

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
