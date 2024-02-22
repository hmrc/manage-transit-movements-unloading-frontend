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

import generated.{AddressType10, HolderOfTheTransitProcedureType06}
import models.reference.Country
import pages.holderOfTheTransitProcedure.CountryPage

class HolderOfTheTransitProcedureHelperSpec extends AnswersHelperSpecBase {

  "HolderOfTheTransitProcedureHelper" - {

    val holderOfTheTransitProcedure = createHotP()

    "section" - {
      "must return empty when HolderOfTheTransitProcedure is undefined" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = None))

        val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
        helper.holderOfTheTransitProcedureSection mustBe Seq()
      }

      "must return section title and rows when HolderOfTheTransitProcedure is defined" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = Some(holderOfTheTransitProcedure)))
          .setValue(CountryPage, Country("GB", "Great Britain"))

        val helper  = new HolderOfTheTransitProcedureHelper(userAnswers)
        val section = helper.holderOfTheTransitProcedureSection.head

        section.sectionTitle.value mustBe "Transit holder"
        section.rows.size mustBe 5
        section.viewLink must not be defined
      }
    }

    "identificationNumber" - {
      "must return None when field is undefined" in {
        val hotP = createHotP(identificationNumber = None)
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = Some(hotP)))

        val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
        helper.identificationNumber(hotP) mustBe None
      }

      "must return Some(Row) when field is defined" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = Some(holderOfTheTransitProcedure)))

        val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
        val result = helper.identificationNumber(holderOfTheTransitProcedure).value

        result.key.value mustBe "EORI number"
        result.value.value mustBe "identificationNumber"
        result.actions mustBe None
      }
    }

    "tirHolderIdentificationNumber" - {
      "must return None when field is undefined" in {
        val hotP = createHotP(tirHolderIdentificationNumber = None)
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = Some(hotP)))

        val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
        helper.tirHolderIdentificationNumber(hotP) mustBe None
      }

      "must return Some(Row) when field is defined" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = Some(holderOfTheTransitProcedure)))

        val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
        val result = helper.tirHolderIdentificationNumber(holderOfTheTransitProcedure).value

        result.key.value mustBe "TIR holder’s identification number"
        result.value.value mustBe "TIRHolderIdentificationNumber"
        result.actions mustBe None
      }
    }

    "country" - {
      "must return None when page is undefined" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = Some(holderOfTheTransitProcedure)))

        val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
        helper.country mustBe None
      }

      "must return Some(Row) when field is defined" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = Some(holderOfTheTransitProcedure)))
          .setValue(CountryPage, Country("GB", "Great Britain"))

        val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
        val result = helper.country.value

        result.key.value mustBe "Country"
        result.value.value mustBe "Great Britain"
        result.actions mustBe None
      }
    }

    "name must return Some(Row)" in {
      val userAnswers = emptyUserAnswers
        .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = Some(holderOfTheTransitProcedure)))

      val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
      val result = helper.name(holderOfTheTransitProcedure).value

      result.key.value mustBe "Name"
      result.value.value mustBe "name"
      result.actions mustBe None
    }

    "address must return Some(Row)" in {
      val userAnswers = emptyUserAnswers
        .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = Some(holderOfTheTransitProcedure)))

      val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
      val result = helper.address(holderOfTheTransitProcedure).value

      result.key.value mustBe "Address"
      result.value.value mustBe "streetAndNumber<br>city<br>postcode"
      result.actions mustBe None
    }
  }

  private def createHotP(
    identificationNumber: Option[String] = Some("identificationNumber"),
    tirHolderIdentificationNumber: Option[String] = Some("TIRHolderIdentificationNumber"),
    name: String = "name",
    streetAndNumber: String = "streetAndNumber",
    postcode: Option[String] = Some("postcode"),
    city: String = "city",
    country: String = "GB"
  ) =
    HolderOfTheTransitProcedureType06(
      identificationNumber,
      tirHolderIdentificationNumber,
      name,
      AddressType10(streetAndNumber, postcode, city, country)
    )
}
