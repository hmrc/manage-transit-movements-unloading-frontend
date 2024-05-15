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

import models.reference._
import models.{CheckMode, DynamicAddress, Index, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages._
import pages.houseConsignment.consignor.CountryPage
import pages.houseConsignment.index.CountryOfDestinationPage
import pages.houseConsignment.index.departureMeansOfTransport.{TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import utils.answersHelpers.AnswersHelperSpecBase
import viewModels.sections.Section.AccordionSection

class HouseConsignmentAnswersHelperSpec extends AnswersHelperSpecBase {

  "grossMassRow" - {
    import pages.houseConsignment.index.GrossWeightPage

    "must return None" - {
      s"when no transport equipments defined" in {
        val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, hcIndex)
        val result = helper.grossMassRow
        result.isEmpty mustBe true
      }
    }

    "must return Some(Row)" - {
      s"when $GrossWeightPage is defined" in {
        val answers = emptyUserAnswers
          .setValue(GrossWeightPage(hcIndex), BigDecimal(999.99))

        val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
        val result = helper.grossMassRow.value

        result.key.value mustBe "Gross weight"
        result.value.value mustBe "999.99kg"
        val action = result.actions.value.items.head
        action.content.value mustBe "Change"
        action.href mustBe "/manage-transit-movements/unloading/AB123/change-house-consignment/1/gross-weight"
        action.visuallyHiddenText.value mustBe "gross weight"
        action.id mustBe "change-gross-mass"
      }
    }
  }

  "preGrossMassRow" - {
    import pages.houseConsignment.index.GrossWeightPage

    "must return None" - {
      s"when no transport equipments defined" in {
        val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, hcIndex)
        val result = helper.preGrossMassRow
        result.isEmpty mustBe true
      }
    }

    "must return Some(Row)" - {
      s"when $GrossWeightPage is defined" in {
        val answers = emptyUserAnswers
          .setValue(GrossWeightPage(hcIndex), BigDecimal(999.99))

        val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
        val result = helper.preGrossMassRow.value

        result.key.value mustBe "Gross weight"
        result.value.value mustBe "999.99"
        result.actions mustBe None
      }
    }
  }

  "HouseConsignmentAnswersHelper" - {

    "consignorName" - {
      val page = ConsignorNamePage(hcIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, hcIndex)
          helper.consignorName() mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
              val result = helper.consignorName().value

              result.key.value mustBe "Name"
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
          helper.consignorIdentification() mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
              val result = helper.consignorIdentification().value

              result.key.value mustBe "EORI number or Trader Identification Number (TIN)"
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

              result.key.value mustBe "Name"
              result.value.value mustBe value
              result.actions must not be defined
          }
        }
      }
    }

    "consignorAddress" - {
      val page = ConsignorAddressPage(hcIndex)
      "must return None" - {
        "when address undefined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, houseConsignmentIndex)
          helper.consignorAddress mustBe None
        }
      }

      "must return Some(row)" - {
        "when address defined" in {
          forAll(arbitrary[DynamicAddress]) {
            address =>
              val answers = emptyUserAnswers.setValue(page, address)

              val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
              val result = helper.consignorAddress.value

              result.key.value mustBe "Address"
              result.value.value mustBe address.toString
              result.actions must not be defined
          }
        }
      }
    }

    "country" - {
      val page = CountryPage(houseConsignmentIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, houseConsignmentIndex)
          helper.consignorCountry mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAnswersHelper(answers, houseConsignmentIndex)
              val result = helper.consignorCountry.value

              result.key.value mustBe "Country"
              result.value.value mustBe value.toString
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

              result.key.value mustBe "EORI number or Trader Identification Number (TIN)"
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

    "documentSections" - {
      import pages.houseConsignment.index.documents.{AdditionalInformationPage, DocumentReferenceNumberPage, TypePage}
      "must generate accordion sections" in {
        forAll(arbitrary[DocumentType], Gen.alphaNumStr, Gen.alphaNumStr) {
          (docType, reference, additionalInfo) =>
            val answers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(hcIndex, Index(0)), reference)
              .setValue(AdditionalInformationPage(hcIndex, Index(0)), additionalInfo)
              .setValue(TypePage(hcIndex, Index(0)), docType)

            val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
            val result = helper.documentSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustBe "Documents"
            result.children.size mustBe 1

            result.viewLinks.size mustBe 1
            val addOrRemoveLink = result.viewLinks.head
            addOrRemoveLink.id mustBe "add-remove-document"
            addOrRemoveLink.text mustBe "Add or remove document"
            addOrRemoveLink.visuallyHidden mustBe defined
            addOrRemoveLink.href mustBe
              controllers.houseConsignment.index.documents.routes.AddAnotherDocumentController.onPageLoad(arrivalId, houseConsignmentIndex, NormalMode).url

            val doc1 = result.children.head
            doc1 mustBe a[AccordionSection]
            doc1.sectionTitle.value mustBe "Document 1"
            doc1.id.value mustBe "document-1"
            doc1.rows.size mustBe 3

            doc1.rows.head.value.value mustBe docType.toString
            doc1.rows(1).value.value mustBe reference
            doc1.rows(2).value.value mustBe additionalInfo
        }
      }
    }

    "departureTransportMeansSections" - {
      "must generate accordion sections" in {
        forAll(arbitrary[TransportMeansIdentification], Gen.alphaNumStr, arbitrary[Country]) {
          (`type`, number, country) =>
            val answers = emptyUserAnswers
              .setValue(TransportMeansIdentificationPage(hcIndex, dtmIndex), `type`)
              .setValue(VehicleIdentificationNumberPage(hcIndex, dtmIndex), number)
              .setValue(pages.houseConsignment.index.departureMeansOfTransport.CountryPage(hcIndex, dtmIndex), country)

            val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
            val result = helper.departureTransportMeansSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustBe "Departure means of transport"
            result.children.size mustBe 1

            result.viewLinks.size mustBe 1
            val addOrRemoveLink = result.viewLinks.head
            addOrRemoveLink.id mustBe "add-remove-departure-transport-means"
            addOrRemoveLink.text mustBe "Add or remove departure means of transport"
            addOrRemoveLink.visuallyHidden must not be defined
            addOrRemoveLink.href mustBe controllers.houseConsignment.index.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController
              .onPageLoad(arrivalId, houseConsignmentIndex, CheckMode)
              .url

            val dtm1 = result.children.head
            dtm1 mustBe a[AccordionSection]
            dtm1.sectionTitle.value mustBe "Departure means of transport 1"
            dtm1.id.value mustBe "departureTransportMeans1"
            dtm1.rows.size mustBe 3

            dtm1.rows.head.value.value mustBe `type`.description
            dtm1.rows(1).value.value mustBe number
            dtm1.rows(2).value.value mustBe country.description
        }
      }
    }

    "additionalInformationSections" - {
      import pages.houseConsignment.index.additionalinformation._

      "must generate accordion sections" in {
        forAll(arbitrary[AdditionalInformationCode], nonEmptyString) {
          (code, description) =>
            val answers = emptyUserAnswers
              .setValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, additionalInformationIndex), code)
              .setValue(HouseConsignmentAdditionalInformationTextPage(hcIndex, additionalInformationIndex), description)

            val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
            val result = helper.additionalInformationSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustBe "Additional information"
            result.children.size mustBe 1

            val additionalInfo1 = result.children.head
            additionalInfo1 mustBe a[AccordionSection]
            additionalInfo1.sectionTitle.value mustBe "Additional information 1"
            additionalInfo1.id.value mustBe "additionalInformation1"
            additionalInfo1.rows.size mustBe 2

            additionalInfo1.rows.head.value.value mustBe code.toString
            additionalInfo1.rows(1).value.value mustBe description
        }
      }
    }

    "additionalReferencesSections" - {
      import pages.houseConsignment.index.additionalReference._

      "must generate accordion sections" in {
        forAll(arbitrary[AdditionalReferenceType], nonEmptyString) {
          (code, description) =>
            val answers = emptyUserAnswers
              .setValue(HouseConsignmentAdditionalReferenceTypePage(hcIndex, additionalInformationIndex), code)
              .setValue(HouseConsignmentAdditionalReferenceNumberPage(hcIndex, additionalInformationIndex), description)

            val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
            val result = helper.additionalReferencesSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustBe "Additional references"
            result.children.size mustBe 1

            result.viewLinks.size mustBe 1
            val addOrRemoveLink = result.viewLinks.head
            addOrRemoveLink.id mustBe "add-remove-additional-reference"
            addOrRemoveLink.text mustBe "Add or remove additional reference"
            addOrRemoveLink.visuallyHidden must not be defined
            addOrRemoveLink.href mustBe controllers.houseConsignment.index.additionalReference.routes.AddAnotherAdditionalReferenceController
              .onSubmit(arrivalId, CheckMode, houseConsignmentIndex)
              .url

            val additionalInfo1 = result.children.head
            additionalInfo1 mustBe a[AccordionSection]
            additionalInfo1.sectionTitle.value mustBe "Additional reference 1"
            additionalInfo1.id.value mustBe "additionalReference1"
            additionalInfo1.rows.size mustBe 2

            additionalInfo1.rows.head.value.value mustBe code.toString
            additionalInfo1.rows(1).value.value mustBe description
        }
      }
    }

    "itemSections" - {
      import pages.houseConsignment.index.items.additionalReference._
      import pages.houseConsignment.index.items.additionalinformation._
      import pages.houseConsignment.index.items.document._
      import pages.houseConsignment.index.items.packages._
      import pages.houseConsignment.index.items.{ConsigneeIdentifierPage => ItemConsigneeIdentifierPage, ConsigneeNamePage => ItemConsigneeNamePage, _}

      "must generate accordion sections" in {
        val description           = Gen.alphaNumStr.sample.value
        val grossWeight           = arbitrary[BigDecimal].sample.value
        val netWeight             = arbitrary[BigDecimal].sample.value
        val packageType           = arbitrary[PackageType].sample.value
        val count                 = arbitrary[BigInt].sample.value
        val additionalReference   = arbitrary[AdditionalReferenceType].sample.value
        val cusCode               = "cusCode"
        val commodityCode         = "commodityCode"
        val nomenclatureCode      = "nomenclatureCode"
        val additionalInformation = AdditionalInformationCode("code", "description")

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
          .setValue(AdditionalReferenceTypePage(hcIndex, itemIndex, additionalReferenceIndex), additionalReference)
          .setValue(HouseConsignmentItemAdditionalInformationCodePage(hcIndex, itemIndex, additionalInformationIndex), additionalInformation)
          .setValue(ItemConsigneeNamePage(hcIndex, itemIndex), "John Smith")
          .setValue(ItemConsigneeIdentifierPage(hcIndex, itemIndex), "csgee1")
          .setValue(DangerousGoodsPage(hcIndex, itemIndex, Index(0)), "dg1")
          .setValue(DangerousGoodsPage(hcIndex, itemIndex, Index(1)), "dg2")

        val helper = new HouseConsignmentAnswersHelper(answers, hcIndex)
        val result = helper.itemSection

        result.viewLinks.size mustBe 1
        val addOrRemoveLink = result.viewLinks.head
        addOrRemoveLink.id mustBe "add-remove-items"
        addOrRemoveLink.text mustBe "Add or remove item"
        addOrRemoveLink.visuallyHidden must not be defined
        addOrRemoveLink.href mustBe "/manage-transit-movements/unloading/AB123/change-house-consignment/1/items/add-another"

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

        result.children.head.children.head.sectionTitle.get mustBe "UN numbers"
        result.children.head.children.head.rows.size mustBe 2
        result.children.head.children.head.rows.head.value.value mustBe "dg1"
        result.children.head.children.head.rows(1).value.value mustBe "dg2"

        result.children.head.children(1).sectionTitle.get mustBe "Consignee"
        result.children.head.children(1).rows.size mustBe 2
        result.children.head.children(1).rows.head.value.value mustBe "csgee1"
        result.children.head.children(1).rows(1).value.value mustBe "John Smith"

        result.children.head.children(2) mustBe a[AccordionSection]
        result.children.head.children(2).sectionTitle.value mustBe "Documents"
        result.children.head.children(2).id.value mustBe "item-1-documents"
        result.children.head.children(2).rows.size mustBe 0
        result.children.head.children(2).viewLinks must not be empty

        result.children.head.children(2).children.head mustBe a[AccordionSection]
        result.children.head.children(2).children.head.sectionTitle.value mustBe "Document 1"
        result.children.head.children(2).children.head.id.value mustBe "item-1-document-1"
        result.children.head.children(2).children.head.rows.size mustBe 1
        result.children.head.children(2).children.head.rows.head.value.value mustBe "doc 1 ref"

        result.children.head.children(2).children(1) mustBe a[AccordionSection]
        result.children.head.children(2).children(1).sectionTitle.value mustBe "Document 2"
        result.children.head.children(2).children(1).id.value mustBe "item-1-document-2"
        result.children.head.children(2).children(1).rows.size mustBe 1
        result.children.head.children(2).children(1).rows.head.value.value mustBe "doc 2 ref"

        result.children.head.children(3) mustBe a[AccordionSection]
        result.children.head.children(3).sectionTitle.value mustBe "Additional references"
        result.children.head.children(3).id.value mustBe "item-1-additional-references"
        result.children.head.children(3).rows.size mustBe 0
        result.children.head.children(3).viewLinks must not be empty

        result.children.head.children(3).children.head mustBe a[AccordionSection]
        result.children.head.children(3).children.head.sectionTitle.value mustBe "Additional reference 1"
        result.children.head.children(3).children.head.id.value mustBe "item-1-additional-reference-1"
        result.children.head.children(3).children.head.rows.size mustBe 1
        result.children.head.children(3).children.head.rows.head.value.value mustBe additionalReference.toString

        result.children.head.children(4) mustBe a[AccordionSection]
        result.children.head.children(4).sectionTitle.value mustBe "Additional information"
        result.children.head.children(4).id.value mustBe "item-1-additional-information"
        result.children.head.children(4).rows.size mustBe 0
        result.children.head.children(4).viewLinks mustBe empty

        result.children.head.children(4).children.head mustBe a[AccordionSection]
        result.children.head.children(4).children.head.sectionTitle.value mustBe "Additional information 1"
        result.children.head.children(4).children.head.id.value mustBe "item-1-additional-information-1"
        result.children.head.children(4).children.head.rows.size mustBe 1
        result.children.head.children(4).children.head.rows.head.value.value mustBe additionalInformation.toString

        result.children.head.children(5) mustBe a[AccordionSection]
        result.children.head.children(5).sectionTitle.value mustBe "Packages"
        result.children.head.children(5).id.value mustBe "item-1-packages"
        result.children.head.children(5).rows.size mustBe 0
        result.children.head.children(5).viewLinks must not be empty

        result.children.head.children(5).children.head.sectionTitle.get mustBe "Package 1"
        result.children.head.children(5).children.head.id.get mustBe "item-1-package-1"
        result.children.head.children(5).children.head.rows.size mustBe 3
        result.children.head.children(5).children.head.rows(0).value.value mustBe s"${packageType.description}"
        result.children.head.children(5).children.head.rows(1).value.value mustBe s"$count"
        result.children.head.children(5).children.head.rows(2).value.value mustBe s"$description"
      }
    }

    "countryOfDestination" - {
      val page = CountryOfDestinationPage(hcIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAnswersHelper(emptyUserAnswers, hcIndex)
          helper.countryOfDestination mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          val answers = emptyUserAnswers.setValue(page, Country("FR", "France"))
          val helper  = new HouseConsignmentAnswersHelper(answers, hcIndex)
          val result  = helper.countryOfDestination.value

          result.key.value mustBe "Country of destination"
          result.value.value mustBe "France"
          result.actions must not be defined
        }
      }
    }
  }
}
