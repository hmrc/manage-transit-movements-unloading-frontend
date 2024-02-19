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

import generated.TraderAtDestinationType03
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.{AdditionalReferenceType, Country}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.additionalReference._
import pages.departureMeansOfTransport._
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import pages._
import viewModels.sections.Section.{AccordionSection, StaticSection}

class ConsignmentAnswersHelperSpec extends AnswersHelperSpecBase {

  "ConsignmentAnswersHelper" - {

    "headerSection" - {
      "must return static section" in {
        forAll(arbitrary[TraderAtDestinationType03]) {
          traderAtDestination =>
            val ie043   = basicIe043.copy(TraderAtDestination = traderAtDestination)
            val answers = emptyUserAnswers.copy(ie043Data = ie043)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.headerSection

            result mustBe a[StaticSection]
            result.rows.head.value.value mustBe traderAtDestination.identificationNumber
        }
      }
    }

    "traderAtDestinationRow" - {
      "must return row" in {
        forAll(Gen.alphaNumStr) {
          value =>
            val traderAtDestination = TraderAtDestinationType03(value)
            val ie043               = basicIe043.copy(TraderAtDestination = traderAtDestination)
            val answers             = emptyUserAnswers.copy(ie043Data = ie043)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.traderAtDestinationRow

            result.key.value mustBe "Authorised consignee’s EORI number or Trader Identification Number (TIN)"
            result.value.value mustBe value
            result.actions must not be defined
        }
      }
    }

    "departureTransportMeansSections" - {
      "must generate accordion sections" in {
        forAll(arbitrary[TransportMeansIdentification], Gen.alphaNumStr, arbitrary[Country]) {
          (`type`, number, country) =>
            val answers = emptyUserAnswers
              .setValue(TransportMeansIdentificationPage(dtmIndex), `type`)
              .setValue(VehicleIdentificationNumberPage(dtmIndex), number)
              .setValue(CountryPage(dtmIndex), country)

            val helper = new ConsignmentAnswersHelper(answers)
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

    "transportEquipmentSections" - {
      "must generate accordion sections" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (containerId, sealId) =>
            val answers = emptyUserAnswers
              .setValue(ContainerIdentificationNumberPage(equipmentIndex), containerId)
              .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), sealId)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.transportEquipmentSections

            result.head mustBe a[AccordionSection]
            result.head.sectionTitle.value mustBe "Transport equipment 1"
            result.head.rows.size mustBe 2
            result.head.rows.head.value.value mustBe containerId
            result.head.rows(1).value.value mustBe sealId
        }
      }
    }

    "additionalReferencesSections" - {
      "must generate accordion sections" in {
        forAll(arbitrary[AdditionalReferenceType], Gen.alphaNumStr) {
          (`type`, number) =>
            val answers = emptyUserAnswers
              .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), `type`)
              .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), number)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.additionalReferencesSections

            result.head mustBe a[AccordionSection]
            result.head.sectionTitle.value mustBe "Additional references"
            result.head.rows.size mustBe 1
            result.head.rows.head.value.value mustBe s"${`type`} - $number"
        }
      }
    }

    "houseConsignmentSections" - {
      "must generate accordion sections" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
          (consignorName, consignorId, consigneeName, consigneeId) =>
            val answers = emptyUserAnswers
              .setValue(ConsignorNamePage(hcIndex), consignorName)
              .setValue(ConsignorIdentifierPage(hcIndex), consignorId)
              .setValue(ConsigneeNamePage(hcIndex), consigneeName)
              .setValue(ConsigneeIdentifierPage(hcIndex), consigneeId)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.houseConsignmentSections

            result.head mustBe a[AccordionSection]
            result.head.sectionTitle.value mustBe "House consignment 1"
            result.head.rows.size mustBe 4
            result.head.rows.head.value.value mustBe consignorName
            result.head.rows(1).value.value mustBe consignorId
            result.head.rows(2).value.value mustBe consigneeName
            result.head.rows(3).value.value mustBe consigneeId
            val link = result.head.viewLink.value
            link.id mustBe "view-house-consignment-1"
            link.text mustBe "View"
            link.href mustBe controllers.routes.HouseConsignmentController.onPageLoad(answers.id, hcIndex).url
            link.visuallyHidden mustBe "on house consignment 1"
            result.head.id.value mustBe "houseConsignment1"
        }
      }
    }
  }
}
