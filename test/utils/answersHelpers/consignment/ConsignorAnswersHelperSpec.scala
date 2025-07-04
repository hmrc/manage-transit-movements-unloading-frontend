/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.answersHelpers.consignment

import generated.AddressType14
import models.DynamicAddress
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.consignor.*
import utils.answersHelpers.AnswersHelperSpecBase

class ConsignorAnswersHelperSpec extends AnswersHelperSpecBase {

  "ConsignorAnswersHelper" - {

    "identificationNumber" - {
      "must return None" - {
        "when identification number undefined" in {
          val helper = new ConsignorAnswersHelper(emptyUserAnswers)
          helper.identificationNumber(None) mustBe None
        }
      }

      "must return Some(row)" - {
        "when identification number defined" in {
          forAll(Gen.alphaNumStr) {
            identificationNumber =>
              val helper = new ConsignorAnswersHelper(emptyUserAnswers)
              val result = helper.identificationNumber(Some(identificationNumber)).value

              result.key.value mustBe "EORI number or Trader Identification Number (TIN)"
              result.value.value mustBe identificationNumber
              result.actions must not be defined
          }
        }
      }
    }

    "name" - {
      "must return None" - {
        "when name undefined" in {
          val helper = new ConsignorAnswersHelper(emptyUserAnswers)
          helper.name(None) mustBe None
        }
      }

      "must return Some(row)" - {
        "when name defined" in {
          forAll(Gen.alphaNumStr) {
            name =>
              val helper = new ConsignorAnswersHelper(emptyUserAnswers)
              val result = helper.name(Some(name)).value

              result.key.value mustBe "Name"
              result.value.value mustBe name
              result.actions must not be defined
          }
        }
      }
    }

    "country" - {
      val page = CountryPage
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignorAnswersHelper(emptyUserAnswers)
          helper.country mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignorAnswersHelper(answers)
              val result = helper.country.value

              result.key.value mustBe "Country"
              result.value.value mustBe value.toString
              result.actions must not be defined
          }
        }
      }
    }

    "address" - {
      "must return None" - {
        "when address undefined" in {
          val helper = new ConsignorAnswersHelper(emptyUserAnswers)
          helper.address(None) mustBe None
        }
      }

      "must return Some(row)" - {
        "when address defined" in {
          forAll(arbitrary[AddressType14]) {
            address =>
              val helper = new ConsignorAnswersHelper(emptyUserAnswers)
              val result = helper.address(Some(address)).value

              result.key.value mustBe "Address"
              result.value.value mustBe DynamicAddress(address).toString
              result.actions must not be defined
          }
        }
      }
    }
  }
}
