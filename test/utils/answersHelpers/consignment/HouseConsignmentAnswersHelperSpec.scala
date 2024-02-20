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

import models.departureTransportMeans.TransportMeansIdentification
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages._
import pages.houseConsignment.index.items.{GrossWeightPage, ItemDescriptionPage}
import utils.answersHelpers.AnswersHelperSpecBase
import viewModels.sections.Section.AccordionSection

class HouseConsignmentAnswersHelperSpec extends AnswersHelperSpecBase {

  "HouseConsignmentAnswersHelper" - {

    "consignorName" - {
      val page = ConsignorNamePage(hcIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, hcIndex)
          helper.consignorName mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
              val result = helper.consignorName.value

              result.key.value mustBe "Consignor name"
              result.value.value mustBe value
              result.actions must not be defined
          }
        }
      }
    }

    "consignorIdentification" - {
      val page = ConsignorIdentifierPage(hcIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, hcIndex)
          helper.consignorIdentification mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
              val result = helper.consignorIdentification.value

              result.key.value mustBe "Consignor EORI number or Trader Identification Number (TIN)"
              result.value.value mustBe value
              result.actions must not be defined
          }
        }
      }
    }

    "consigneeName" - {
      val page = ConsigneeNamePage(hcIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, hcIndex)
          helper.consigneeName mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
              val result = helper.consigneeName.value

              result.key.value mustBe "Consignee name"
              result.value.value mustBe value
              result.actions must not be defined
          }
        }
      }
    }

    "consigneeIdentification" - {
      val page = ConsigneeIdentifierPage(hcIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, hcIndex)
          helper.consigneeIdentification mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
              val result = helper.consigneeIdentification.value

              result.key.value mustBe "Consignee EORI number or Trader Identification Number (TIN)"
              result.value.value mustBe value
              result.actions must not be defined
          }
        }
      }
    }

    "departureTransportMeansSections" - {
      "must generate accordion sections" in {
        forAll(arbitrary[TransportMeansIdentification], Gen.alphaNumStr, arbitrary[Country]) {
          (`type`, number, country) =>
            val answers = emptyUserAnswers
              .setValue(DepartureTransportMeansIdentificationTypePage(hcIndex, dtmIndex), `type`)
              .setValue(DepartureTransportMeansIdentificationNumberPage(hcIndex, dtmIndex), number)
              .setValue(DepartureTransportMeansCountryPage(hcIndex, dtmIndex), country)

            val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
            val result = helper.departureTransportMeansSections

            result.head mustBe a[AccordionSection]
            result.head.sectionTitle.value mustBe "Departure means of transport 1"
            result.head.rows.size mustBe 3
            result.head.rows.head.value.value mustBe `type`.description
            result.head.rows(1).value.value mustBe number
            result.head.rows(2).value.value mustBe country.description
        }
      }
    }

    "itemSections" - {
      "must generate accordion sections" in {
        forAll(Gen.alphaNumStr, arbitrary[BigDecimal], arbitrary[Double]) {
          (description, grossWeight, netWeight) =>
            val answers = emptyUserAnswers
              .setValue(ItemDescriptionPage(hcIndex, itemIndex), description)
              .setValue(GrossWeightPage(hcIndex, itemIndex), grossWeight)
              .setValue(NetWeightPage(hcIndex, itemIndex), netWeight)

            val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
            val result = helper.itemSections

            result.head mustBe a[AccordionSection]
            result.head.sectionTitle.value mustBe "Item 1"
            result.head.rows.size mustBe 3
            result.head.rows.head.value.value mustBe description
            result.head.rows(1).value.value mustBe s"${grossWeight}kg"
            result.head.rows(2).value.value mustBe s"${netWeight}kg"
        }
      }
    }
  }
}
