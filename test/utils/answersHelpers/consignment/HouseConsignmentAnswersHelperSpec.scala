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
import models.reference.{AdditionalReferenceType, Country, PackageType}
import models.{DynamicAddress, Index}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages._
import pages.houseConsignment.index.items.additionalReference.AdditionalReferencePage
import pages.houseConsignment.index.items.document.DocumentReferenceNumberPage
import pages.houseConsignment.index.items.packaging.{PackagingCountPage, PackagingMarksPage, PackagingTypePage}
import pages.houseConsignment.index.items.{
  CombinedNomenclatureCodePage,
  CommodityCodePage,
  CustomsUnionAndStatisticsCodePage,
  GrossWeightPage,
  ItemDescriptionPage,
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
            val (cusCode, commodityCode, nomenclatureCode) = ("cusCode", "commodityCode", "nomenclatureCode")
            val answers = emptyUserAnswers
              .setValue(ItemDescriptionPage(hcIndex, itemIndex), description)
              .setValue(GrossWeightPage(hcIndex, itemIndex), grossWeight)
              .setValue(NetWeightPage(hcIndex, itemIndex), netWeight)
              .setValue(PackagingTypePage(hcIndex, itemIndex, packageIndex), packageType)
              .setValue(PackagingCountPage(hcIndex, itemIndex, packageIndex), count)
              .setValue(PackagingMarksPage(hcIndex, itemIndex, packageIndex), description)
              .setValue(CustomsUnionAndStatisticsCodePage(hcIndex, itemIndex), cusCode)
              .setValue(CommodityCodePage(hcIndex, itemIndex), commodityCode)
              .setValue(CombinedNomenclatureCodePage(hcIndex, itemIndex), nomenclatureCode)
              .setValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(0)), "doc 1 ref")
              .setValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(1)), "doc 2 ref")
              .setValue(AdditionalReferencePage(hcIndex, itemIndex, additionalReferenceIndex), additionalReference)
              .setValue(ItemConsigneeNamePage(hcIndex, itemIndex), "John Smith")
              .setValue(ItemConsigneeIdentifierPage(hcIndex, itemIndex), "csgee1")

            val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
            val result = helper.itemSections

            result.head.viewLinks.length mustBe 3

            result.head.viewLinks(0).href mustBe "#"
            result.head.viewLinks(0).id mustBe "add-remove-packaging"
            result.head.viewLinks(0).text mustBe "Add or remove package"

            result.head.viewLinks(1).href mustBe "#"
            result.head.viewLinks(1).id mustBe "add-remove-document"
            result.head.viewLinks(1).text mustBe "Add or remove document"

            result.head.viewLinks(2).href mustBe "#"
            result.head.viewLinks(2).id mustBe "add-remove-additionalReference"
            result.head.viewLinks(2).text mustBe "Add or remove additional reference"

            result.head mustBe a[AccordionSection]
            result.head.sectionTitle.value mustBe "Item 1"
            result.head.rows.size mustBe 6
            result.head.rows.head.value.value mustBe description
            result.head.rows(1).value.value mustBe s"${grossWeight}kg"
            result.head.rows(2).value.value mustBe s"${netWeight}kg"
            result.head.rows(3).value.value mustBe s"$cusCode"
            result.head.rows(4).value.value mustBe s"$commodityCode"
            result.head.rows(5).value.value mustBe s"$nomenclatureCode"

            result.head.children.head.sectionTitle.get mustBe "Consignee"
            result.head.children.head.rows.size mustBe 2
            result.head.children.head.rows.head.value.value mustBe "csgee1"
            result.head.children.head.rows(1).value.value mustBe "John Smith"

            result.head.children(1) mustBe a[AccordionSection]
            result.head.children(1).sectionTitle.value mustBe "Document 1"
            result.head.children(1).rows.size mustBe 1
            result.head.children(1).rows.head.value.value mustBe "doc 1 ref"

            result.head.children(2) mustBe a[AccordionSection]
            result.head.children(2).sectionTitle.value mustBe "Document 2"
            result.head.children(2).rows.size mustBe 1
            result.head.children(2).rows.head.value.value mustBe "doc 2 ref"

            result.head.children(3) mustBe a[AccordionSection]
            result.head.children(3).sectionTitle.value mustBe "Additional reference 1"
            result.head.children(3).rows.size mustBe 1
            result.head.children(3).rows.head.value.value mustBe additionalReference.toString

            result.head.children(4).sectionTitle.get mustBe "Package 1"
            result.head.children(4).rows.size mustBe 3
            result.head.children(4).rows(0).value.value mustBe s"${packageType.asDescription}"
            result.head.children(4).rows(1).value.value mustBe s"$count"
            result.head.children(4).rows(2).value.value mustBe s"$description"
        }
      }
    }
  }
}
