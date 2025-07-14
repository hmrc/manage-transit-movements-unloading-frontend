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

package utils.answersHelpers

import generated.AddressType15
import models.reference.Country
import pages.holderOfTheTransitProcedure.CountryPage

class HolderOfTheTransitProcedureHelperSpec extends AnswersHelperSpecBase {

  "HolderOfTheTransitProcedureHelper" - {

    "identificationNumber" - {
      "must return None when field is undefined" in {
        val helper = new HolderOfTheTransitProcedureHelper(emptyUserAnswers)
        helper.identificationNumber(None) must not be defined
      }

      "must return Some(Row) when field is defined" in {
        val helper = new HolderOfTheTransitProcedureHelper(emptyUserAnswers)
        val result = helper.identificationNumber(Some("identificationNumber")).value

        result.key.value mustEqual "EORI number"
        result.value.value mustEqual "identificationNumber"
        result.actions must not be defined
      }
    }

    "tirHolderIdentificationNumber" - {
      "must return None when field is undefined" in {
        val helper = new HolderOfTheTransitProcedureHelper(emptyUserAnswers)
        helper.tirHolderIdentificationNumber(None) must not be defined
      }

      "must return Some(Row) when field is defined" in {
        val helper = new HolderOfTheTransitProcedureHelper(emptyUserAnswers)
        val result = helper.tirHolderIdentificationNumber(Some("TIRHolderIdentificationNumber")).value

        result.key.value mustEqual "TIR holder’s identification number"
        result.value.value mustEqual "TIRHolderIdentificationNumber"
        result.actions must not be defined
      }
    }

    "country" - {
      "must return None when page is undefined" in {
        val helper = new HolderOfTheTransitProcedureHelper(emptyUserAnswers)
        helper.country must not be defined
      }

      "must return Some(Row) when field is defined" in {
        val userAnswers = emptyUserAnswers.setValue(CountryPage, Country("GB", "Great Britain"))
        val helper      = new HolderOfTheTransitProcedureHelper(userAnswers)
        val result      = helper.country.value

        result.key.value mustEqual "Country"
        result.value.value mustEqual "Great Britain"
        result.actions must not be defined
      }
    }

    "name must return Some(Row)" in {
      val helper = new HolderOfTheTransitProcedureHelper(emptyUserAnswers)
      val result = helper.name("name").value

      result.key.value mustEqual "Name"
      result.value.value mustEqual "name"
      result.actions must not be defined
    }

    "address must return Some(Row)" in {
      val helper = new HolderOfTheTransitProcedureHelper(emptyUserAnswers)
      val result = helper.address(AddressType15("streetAndNumber", Some("postcode"), "city", "GB")).value

      result.key.value mustEqual "Address"
      result.value.value mustEqual "streetAndNumber<br>city<br>postcode"
      result.actions must not be defined
    }
  }
}
