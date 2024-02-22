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

import generated.{CC043CType, Number0, TraderAtDestinationType03, TransitOperationType14}
import generators.Generators
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.{AdditionalReferenceType, Country, CustomsOffice}
import models.{DeclarationType, SecurityType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages._
import pages.additionalReference._
import pages.departureMeansOfTransport._
import pages.houseConsignment.index.items.{CombinedNomenclatureCodePage, CommodityCodePage, CustomsUnionAndStatisticsCodePage}
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import scalaxb.XMLCalendar
import viewModels.sections.Section.{AccordionSection, StaticSection}

class ConsignmentAnswersHelperSpec extends AnswersHelperSpecBase with Generators {

  "ConsignmentAnswersHelper" - {

    "headerSection" - {
      "must return static section" in {
        forAll(arbitrary[TraderAtDestinationType03], arbitrary[CustomsOffice]) {
          (traderAtDestination, arbitraryCustomsOffice) =>
            val ie043   = basicIe043.copy(TraderAtDestination = traderAtDestination)
            val answers = emptyUserAnswers.copy(ie043Data = ie043).setValue(CustomsOfficeOfDestinationActualPage, arbitraryCustomsOffice)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.headerSection

            result mustBe a[StaticSection]
            result.rows.head.value.value mustBe arbitraryCustomsOffice.name
            result.rows(1).value.value mustBe traderAtDestination.identificationNumber
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

    "declarationTypeRow" - {
      "must return row" in {
        forAll(arbitrary[DeclarationType]) {
          value =>
            val decType: TransitOperationType14 = TransitOperationType14("Mrn", Some(value.code), None, "1", Number0)
            val ie043: CC043CType               = basicIe043.copy(TransitOperation = decType)
            val answers: UserAnswers            = emptyUserAnswers.copy(ie043Data = ie043)
            val helper                          = new ConsignmentAnswersHelper(answers)
            val result                          = helper.declarationTypeRow.get

            result.key.value mustBe "Declaration type"
            result.value.value mustBe value.code
            result.actions must not be defined
        }
      }
    }

    "securityTypeRow" - {
      "must return row" in {
        forAll(arbitrary[SecurityType]) {
          value =>
            val answers: UserAnswers = emptyUserAnswers.setValue(SecurityTypePage, value)
            val helper               = new ConsignmentAnswersHelper(answers)
            val result               = helper.securityTypeRow.get

            result.key.value mustBe "Safety and security details"
            result.value.value mustBe value.description
            result.actions must not be defined
        }
      }
    }

    "reducedDatasetIndicatorRow" - {
      "must return row" in {
        val decType: TransitOperationType14 = TransitOperationType14("Mrn", None, None, "1", Number0)
        val ie043: CC043CType               = basicIe043.copy(TransitOperation = decType)
        val answers: UserAnswers            = emptyUserAnswers.copy(ie043Data = ie043)

        val helper = new ConsignmentAnswersHelper(answers)
        val result = helper.reducedDatasetIndicatorRow

        result.key.value mustBe "Do you want to add a reduced data set?"
        result.value.value mustBe "No"
        result.actions must not be defined
      }
    }
    "declarationAcceptanceDate" - {
      "must return row" in {
        val decType: TransitOperationType14 = TransitOperationType14("Mrn", None, Some(XMLCalendar("2023-06-09")), "1", Number0)
        val ie043: CC043CType               = basicIe043.copy(TransitOperation = decType)
        val answers: UserAnswers            = emptyUserAnswers.copy(ie043Data = ie043)

        val helper = new ConsignmentAnswersHelper(answers)
        val result = helper.declarationAcceptanceDateRow.get

        result.key.value mustBe "Declaration acceptance date"
        result.value.value mustBe "9 June 2023"
        result.actions must not be defined
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
            val (cusCode, commodityCode, nomenclatureCode) = ("cusCode", "commodityCode", "nomenclatureCode")
            val answers = emptyUserAnswers
              .setValue(ConsignorNamePage(hcIndex), consignorName)
              .setValue(ConsignorIdentifierPage(hcIndex), consignorId)
              .setValue(ConsigneeNamePage(hcIndex), consigneeName)
              .setValue(ConsigneeIdentifierPage(hcIndex), consigneeId)
              .setValue(CustomsUnionAndStatisticsCodePage(hcIndex, itemIndex), cusCode)
              .setValue(CommodityCodePage(hcIndex, itemIndex), commodityCode)
              .setValue(CombinedNomenclatureCodePage(hcIndex, itemIndex), nomenclatureCode)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.houseConsignmentSections

            result.head mustBe a[AccordionSection]
            result.head.sectionTitle.value mustBe "House consignment 1"
            result.head.rows.size mustBe 4
            result.head.rows.head.value.value mustBe consignorName
            result.head.rows(1).value.value mustBe consignorId
            result.head.rows(2).value.value mustBe consigneeName
            result.head.rows(3).value.value mustBe consigneeId

            result.head.children.head.sectionTitle.value mustBe "Item 1"
            result.head.children.head.rows.size mustBe 3
            result.head.children.head.rows.head.value.value mustBe cusCode
            result.head.children.head.rows(1).value.value mustBe commodityCode
            result.head.children.head.rows(2).value.value mustBe nomenclatureCode

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
