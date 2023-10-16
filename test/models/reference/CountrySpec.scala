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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

class CountrySpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Country" - {

    "when description is present" - {
      "must serialise" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val country = Country(code, Some(description))
            Json.toJson(country) mustBe Json.parse(s"""
                 |{
                 |  "code": "$code",
                 |  "description": "$description"
                 |}
                 |""".stripMargin)
        }
      }

      "must deserialise" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val country = Country(code, Some(description))
            Json
              .parse(s"""
                   |{
                   |  "code": "$code",
                   |  "description": "$description"
                   |}
                   |""".stripMargin)
              .as[Country] mustBe country
        }
      }
    }

    "when description is not present" - {
      "must serialise" in {
        val countryCode = Gen.alphaNumStr.sample.value
        val country     = Country(countryCode, None)
        Json.toJson(country) mustBe Json.parse(s"""
             |{
             |  "code": "$countryCode"
             |}
             |""".stripMargin)
      }

      "must deserialise" in {
        val countryCode = Gen.alphaNumStr.sample.value
        val country     = Country(countryCode, None)
        Json
          .parse(s"""
               |{
               |  "code": "$countryCode"
               |}
               |""".stripMargin)
          .as[Country] mustBe country
      }
    }

    "must convert to select item" in {
      forAll(arbitrary[Country], arbitrary[Boolean]) {
        (country, selected) =>
          country.toSelectItem(selected) mustBe SelectItem(Some(country.code), s"${country.description.getOrElse(None)} - ${country.code}", selected)
      }
    }

    "must format as string" in {
      forAll(arbitrary[Country]) {
        country =>
          country.toString mustBe s"${country.description.getOrElse(None)} - ${country.code}"
      }
    }
  }

}
