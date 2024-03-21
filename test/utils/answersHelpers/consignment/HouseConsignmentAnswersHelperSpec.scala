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
import models.reference.{AdditionalInformationCode, AdditionalReferenceType, Country, PackageType}
import models.{DynamicAddress, Index}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages._
import pages.houseConsignment.index.items.additionalReference.AdditionalReferencePage
import pages.houseConsignment.index.items.additionalinformation.HouseConsignmentAdditionalInformationCodePage
import pages.houseConsignment.index.items.document.DocumentReferenceNumberPage
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageShippingMarkPage, PackageTypePage}
import pages.houseConsignment.index.items.{
  CombinedNomenclatureCodePage,
  CommodityCodePage,
  CustomsUnionAndStatisticsCodePage,
  GrossWeightPage,
  ItemDescriptionPage,
  NetWeightPage,
  ConsigneeIdentifierPage => ItemConsigneeIdentifierPage,
  ConsigneeNamePage => ItemConsigneeNamePage
}
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

    "ConsigneeAddressPage" - {
      val page = ConsigneeAddressPage(hcIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, hcIndex)
          helper.consigneeAddress mustBe None
          helper.consigneeCountry mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[DynamicAddress]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
              val result = helper.consigneeAddress.value

              result.key.value mustBe "Address"
              result.value.value mustBe value.toString
              result.actions must not be defined
          }
        }
      }
    }

    "ConsigneeCountryPage" - {
      val page = ConsigneeCountryPage(hcIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, hcIndex)
          helper.consigneeCountry mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
              val result = helper.consigneeCountry.value

              result.key.value mustBe "Country"
              result.value.value mustBe value.toString
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
            result.head.id.value mustBe "departureTransportMeans1"

            result.head.rows.head.value.value mustBe `type`.description
            result.head.rows(1).value.value mustBe number
            result.head.rows(2).value.value mustBe country.description
        }
      }
    }

    "itemSections" - {
      "must generate accordion sections" in {
        forAll(Gen.alphaNumStr, arbitrary[BigDecimal], arbitrary[Double], arbitrary[PackageType], arbitrary[BigInt], arbitrary[AdditionalReferenceType]) {
          (description, grossWeight, netWeight, packageType, count, additionalReference) =>
            val (cusCode, commodityCode, nomenclatureCode, additionalInformation) =
              ("cusCode", "commodityCode", "nomenclatureCode", AdditionalInformationCode("code", "description"))
            val answers = emptyUserAnswers
              .setValue(ItemDescriptionPage(hcIndex, itemIndex), description)
              .setValue(GrossWeightPage(hcIndex, itemIndex), grossWeight)
              .setValue(NetWeightPage(hcIndex, itemIndex), netWeight)
              .setValue(PackageTypePage(hcIndex, itemIndex, packageIndex), packageType)
              .setValue(NumberOfPackagesPage(hcIndex, itemIndex, packageIndex), count)
              .setValue(PackageShippingMarkPage(hcIndex, itemIndex, packageIndex), description)
              .setValue(CustomsUnionAndStatisticsCodePage(hcIndex, itemIndex), cusCode)
              .setValue(CommodityCodePage(hcIndex, itemIndex), commodityCode)
              .setValue(CombinedNomenclatureCodePage(hcIndex, itemIndex), nomenclatureCode)
              .setValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(0)), "doc 1 ref")
              .setValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(1)), "doc 2 ref")
              .setValue(AdditionalReferencePage(hcIndex, itemIndex, additionalReferenceIndex), additionalReference)
              .setValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, itemIndex, additionalInformationIndex), additionalInformation)
              .setValue(ItemConsigneeNamePage(hcIndex, itemIndex), "John Smith")
              .setValue(ItemConsigneeIdentifierPage(hcIndex, itemIndex), "csgee1")

            val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
            val result = helper.itemSection

            result.viewLinks.length mustBe 1

            result.viewLinks.head.href mustBe "#"
            result.viewLinks.head.id mustBe "add-remove-items"
            result.viewLinks.head.text mustBe "Add or remove item"

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustBe "Items"
            result.rows.size mustBe 0
            result.id.value mustBe "items"

            result.children.head mustBe a[AccordionSection]
            result.children.head.sectionTitle.value mustBe "Item 1"
            result.children.head.id.value mustBe "item-1"
            result.children.head.rows.size mustBe 6
            result.children.head.rows.head.value.value mustBe description
            result.children.head.rows(1).value.value mustBe s"${grossWeight}kg"
            result.children.head.rows(2).value.value mustBe s"${netWeight}kg"
            result.children.head.rows(3).value.value mustBe s"$cusCode"
            result.children.head.rows(4).value.value mustBe s"$commodityCode"
            result.children.head.rows(5).value.value mustBe s"$nomenclatureCode"

            result.children.head.children.head.sectionTitle.get mustBe "Consignee"
            result.children.head.children.head.rows.size mustBe 2
            result.children.head.children.head.rows.head.value.value mustBe "csgee1"
            result.children.head.children.head.rows(1).value.value mustBe "John Smith"

            result.children.head.children(1) mustBe a[AccordionSection]
            result.children.head.children(1).sectionTitle.value mustBe "Documents"
            result.children.head.children(1).id.value mustBe "item-1-documents"
            result.children.head.children(1).rows.size mustBe 0
            result.children.head.children(1).viewLinks must not be empty

            result.children.head.children(1).children.head mustBe a[AccordionSection]
            result.children.head.children(1).children.head.sectionTitle.value mustBe "Document 1"
            result.children.head.children(1).children.head.id.value mustBe "item-1-document-1"
            result.children.head.children(1).children.head.rows.size mustBe 1
            result.children.head.children(1).children.head.rows.head.value.value mustBe "doc 1 ref"

            result.children.head.children(1).children(1) mustBe a[AccordionSection]
            result.children.head.children(1).children(1).sectionTitle.value mustBe "Document 2"
            result.children.head.children(1).children(1).id.value mustBe "item-1-document-2"
            result.children.head.children(1).children(1).rows.size mustBe 1
            result.children.head.children(1).children(1).rows.head.value.value mustBe "doc 2 ref"

            result.children.head.children(2) mustBe a[AccordionSection]
            result.children.head.children(2).sectionTitle.value mustBe "Additional references"
            result.children.head.children(2).id.value mustBe "item-1-additional-references"
            result.children.head.children(2).rows.size mustBe 0
            result.children.head.children(2).viewLinks must not be empty

            result.children.head.children(2).children.head mustBe a[AccordionSection]
            result.children.head.children(2).children.head.sectionTitle.value mustBe "Additional reference 1"
            result.children.head.children(2).children.head.id.value mustBe "item-1-additional-reference-1"
            result.children.head.children(2).children.head.rows.size mustBe 1
            result.children.head.children(2).children.head.rows.head.value.value mustBe additionalReference.toString

            result.children.head.children(3) mustBe a[AccordionSection]
            result.children.head.children(3).sectionTitle.value mustBe "Additional information"
            result.children.head.children(3).id.value mustBe "item-1-additional-information"
            result.children.head.children(3).rows.size mustBe 0
            result.children.head.children(3).viewLinks mustBe empty

            result.children.head.children(3).children.head mustBe a[AccordionSection]
            result.children.head.children(3).children.head.sectionTitle.value mustBe "Additional information 1"
            result.children.head.children(3).children.head.id.value mustBe "item-1-additional-information-1"
            result.children.head.children(3).children.head.rows.size mustBe 1
            result.children.head.children(3).children.head.rows.head.value.value mustBe additionalInformation.toString

            result.children.head.children(4) mustBe a[AccordionSection]
            result.children.head.children(4).sectionTitle.value mustBe "Packages"
            result.children.head.children(4).id.value mustBe "item-1-packages"
            result.children.head.children(4).rows.size mustBe 0
            result.children.head.children(4).viewLinks must not be empty

            result.children.head.children(4).children.head.sectionTitle.get mustBe "Package 1"
            result.children.head.children(4).children.head.id.get mustBe "item-1-package-1"
            result.children.head.children(4).children.head.rows.size mustBe 3
            result.children.head.children(4).children.head.rows(0).value.value mustBe s"${packageType.asDescription}"
            result.children.head.children(4).children.head.rows(1).value.value mustBe s"$count"
            result.children.head.children(4).children.head.rows(2).value.value mustBe s"$description"
        }
      }
    }
  }
}
