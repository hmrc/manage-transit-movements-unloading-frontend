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

package utils.answersHelpers.consignment.houseConsignment

import models.DocType.Previous
import models.reference.{AdditionalReferenceType, Country, DocumentType, PackageType}
import models.{CheckMode, Index, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.houseConsignment.index.items._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import utils.answersHelpers.AnswersHelperSpecBase
import viewModels.sections.Section.AccordionSection

class ConsignmentItemAnswersHelperSpec extends AnswersHelperSpecBase {

  "ConsignmentItemAnswersHelper" - {

    "descriptionRow" - {
      val page = ItemDescriptionPage(hcIndex, itemIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.descriptionRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.descriptionRow.value
              result.value.value mustEqual value
              val actions = result.actions.get.items
              result.key.value mustEqual "Description"
              val action = actions.head
              action.href mustEqual controllers.houseConsignment.index.items.routes.DescriptionController
                .onPageLoad(arrivalId, CheckMode, CheckMode, hcIndex, itemIndex)
                .url
              action.visuallyHiddenText.get mustEqual "description of item 1"

          }
        }
      }
    }

    "ucrRow" - {
      val page = UniqueConsignmentReferencePage(hcIndex, itemIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.ucrRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.ucrRow.value
              result.value.value mustEqual value
              val actions = result.actions.get.items
              result.key.value mustEqual "Reference Number UCR"
              val action = actions.head
              action.href mustEqual controllers.houseConsignment.index.items.routes.UniqueConsignmentReferenceController
                .onPageLoad(arrivalId, CheckMode, CheckMode, hcIndex, itemIndex)
                .url
              action.visuallyHiddenText.get mustEqual "reference number UCR of item 1"

          }
        }
      }
    }

    "declarationTypeRow" - {
      val page = DeclarationTypePage(hcIndex, itemIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.declarationType must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.declarationType.value

              result.key.value mustEqual "Declaration type"
              result.value.value mustEqual value
              result.actions must not be defined
          }
        }
      }
    }

    "countryOfDestinationRow" - {
      val page = CountryOfDestinationPage(hcIndex, itemIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.countryOfDestination must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.countryOfDestination.value

              result.key.value mustEqual "Country of destination"
              result.value.value mustEqual value.description
              result.actions must not be defined
          }
        }
      }
    }

    "grossWeightRow" - {
      val page = GrossWeightPage(hcIndex, itemIndex)
      "must return add link" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.grossWeightRow.key.value mustEqual "Gross weight"

          helper.grossWeightRow.value.content.asHtml.toString() must include("Enter gross weight")
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[BigDecimal]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.grossWeightRow

              result mustEqual
                SummaryListRow(
                  key = Key("Gross weight".toText),
                  value = Value(s"${value}kg".toText),
                  actions = grossWeightAction
                )
          }
        }
      }
    }

    "netWeightRow" - {
      val page = NetWeightPage(hcIndex, itemIndex)
      "must return add link" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.netWeightRow.key.value mustEqual "Net weight"

          helper.netWeightRow.value.content.asHtml.toString() must include("Enter net weight")
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[BigDecimal]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.netWeightRow

              result.key.value mustEqual "Net weight"
              result.value.value mustEqual s"${value}kg"
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.houseConsignment.index.items.routes.NetWeightController
                .onPageLoad(arrivalId, hcIndex, itemIndex, CheckMode, CheckMode)
                .url
              action.visuallyHiddenText.value mustEqual "net weight of item 1"
              action.id mustEqual "change-net-weight-1"

              val action2 = result.actions.value.items(1)
              action2.content.value mustEqual "Remove"
              action2.visuallyHiddenText.value mustEqual "net weight of item 1"
              action2.href mustEqual controllers.houseConsignment.index.items.routes.RemoveNetWeightYesNoController
                .onPageLoad(arrivalId, NormalMode, hcIndex, itemIndex)
                .url
          }
        }
      }
    }

    "cusCodeRow" - {

      val value = Gen.alphaLowerStr.sample.value
      val page  = CustomsUnionAndStatisticsCodePage(hcIndex, itemIndex)

      "must return None" - {
        s"when $page undefined" in {

          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          val result = helper.cusCodeRow
          result must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          val userAnswers = emptyUserAnswers.setValue(page, value)

          val helper = new ConsignmentItemAnswersHelper(userAnswers, hcIndex, itemIndex)
          val result = helper.cusCodeRow.value

          result mustEqual
            SummaryListRow(
              key = Key("Customs Union and Statistics (CUS) code".toText),
              value = Value(s"$value".toText),
              actions = cusCodeItemAction
            )
        }
      }
    }

    "commodityCodeRow" - {

      val value = Gen.alphaLowerStr.sample.value
      val page  = CommodityCodePage(hcIndex, itemIndex)

      "must return add link" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.commodityCodeRow.key.value mustEqual "Commodity code"

          helper.commodityCodeRow.value.content.asHtml.toString() must include("Enter commodity code")
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          val userAnswers = emptyUserAnswers.setValue(page, value)

          val helper = new ConsignmentItemAnswersHelper(userAnswers, hcIndex, itemIndex)
          val result = helper.commodityCodeRow

          result mustEqual
            SummaryListRow(
              key = Key("Commodity code".toText),
              value = Value(s"$value".toText),
              actions = commodityCodeItemAction
            )
        }
      }
    }

    "nomenclatureCodeRow" - {

      val value = Gen.alphaLowerStr.sample.value
      val page  = CombinedNomenclatureCodePage(hcIndex, itemIndex)

      "must return add link" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          val result = helper.nomenclatureCodeRow
          result.key.value mustEqual "Combined nomenclature code"

          result.value.content.asHtml.toString() must include("Enter nomenclature code")
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          val userAnswers = emptyUserAnswers.setValue(page, value)

          val helper = new ConsignmentItemAnswersHelper(userAnswers, hcIndex, itemIndex)
          val result = helper.nomenclatureCodeRow

          result mustEqual
            SummaryListRow(
              key = Key("Combined nomenclature code".toText),
              value = Value(s"$value".toText),
              actions = nomenclatureCodeItemAction
            )
        }
      }
    }

    "documentSections" - {
      import pages.houseConsignment.index.items.document._
      "must generate accordion sections" in {
        forAll(arbitrary[DocumentType], Gen.alphaNumStr, Gen.alphaNumStr) {
          (documentType, referenceNumber, additionalInformation) =>
            val answers = emptyUserAnswers
              .setValue(TypePage(hcIndex, itemIndex, Index(0)), documentType)
              .setValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(0)), referenceNumber)
              .setValue(AdditionalInformationPage(hcIndex, itemIndex, Index(0)), additionalInformation)
              .setValue(TypePage(hcIndex, itemIndex, Index(1)), documentType)
              .setValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(1)), referenceNumber)
              .setValue(AdditionalInformationPage(hcIndex, itemIndex, Index(1)), additionalInformation)
              .setValue(TypePage(hcIndex, itemIndex, Index(2)), documentType.copy(`type` = Previous))
              .setValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(2)), referenceNumber)
              .setValue(AdditionalInformationPage(hcIndex, itemIndex, Index(2)), additionalInformation)

            val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
            val result = helper.documentSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Documents"
            result.id.value mustEqual "item-1-documents"

            val addOrRemoveLink = result.viewLinks.head
            addOrRemoveLink.id mustEqual "add-remove-item-1-document"
            addOrRemoveLink.text mustEqual "Add or remove document"
            addOrRemoveLink.visuallyHidden.value mustEqual "from item 1"
            addOrRemoveLink.href mustEqual controllers.houseConsignment.index.items.document.routes.AddAnotherDocumentController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode, CheckMode)
              .url

            result.children.head mustBe a[AccordionSection]
            result.children.head.sectionTitle.value mustEqual "Document 1"
            result.children.head.id.value mustEqual "item-1-document-1"
            result.children.head.rows.size mustEqual 3

            result.children(1) mustBe a[AccordionSection]
            result.children(1).sectionTitle.value mustEqual "Document 2"
            result.children(1).id.value mustEqual "item-1-document-2"
            result.children(1).rows.size mustEqual 3

            result.children(2) mustBe a[AccordionSection]
            result.children(2).sectionTitle.value mustEqual "Document 3"
            result.children(2).id.value mustEqual "item-1-document-3"
            result.children(2).rows.size mustEqual 3
        }
      }
    }

    "additionalReferencesSections" - {
      import pages.houseConsignment.index.items.additionalReference._

      "must generate accordion sections" in {
        forAll(arbitrary[AdditionalReferenceType], nonEmptyString) {
          (`type`, number) =>
            val answers = emptyUserAnswers
              .setValue(AdditionalReferenceTypePage(additionalReferenceIndex, hcIndex, itemIndex), `type`)
              .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex, hcIndex, itemIndex), number)

            val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex);
            val result = helper.additionalReferencesSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Additional references"
            result.children.size mustEqual 1
            result.children.head.sectionTitle.value mustEqual "Additional reference 1"
            result.children.head.rows.size mustEqual 2
            result.children.head.rows.head.value.value mustEqual `type`.toString
            result.children.head.rows(1).value.value mustEqual number
            result.id.value mustEqual "item-1-additional-references"

            val addOrRemoveLink = result.viewLinks.head
            addOrRemoveLink.id mustEqual "add-remove-item-1-additional-reference"
            addOrRemoveLink.text mustEqual "Add or remove additional reference"
            addOrRemoveLink.visuallyHidden.value mustEqual "from item 1"
            addOrRemoveLink.href mustEqual "/manage-transit-movements/unloading/AB123/change-house-consignment/1/change-item/1/additional-references/add-another"

        }
      }
    }

    "dangerousGoodsSection" - {
      "must generate accordion section" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (value1, value2) =>
            val answers = emptyUserAnswers
              .setValue(DangerousGoodsPage(hcIndex, itemIndex, Index(0)), value1)
              .setValue(DangerousGoodsPage(hcIndex, itemIndex, Index(1)), value2)

            val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
            val result = helper.dangerousGoodsSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "UN numbers"
            result.id.value mustEqual "item-1-dangerous-goods"

            result.viewLinks mustBe empty

            result.rows.size mustEqual 2
            result.rows.head.value.value mustEqual value1
            result.rows(1).value.value mustEqual value2
        }
      }
    }

    "packagingSection" - {
      import pages.houseConsignment.index.items.packages._
      "must generate accordion sections" in {
        forAll(arbitrary[PackageType], arbitrary[BigInt], nonEmptyString) {
          (packageType, quantity, shippingMark) =>
            val answers = emptyUserAnswers
              .setValue(PackageTypePage(hcIndex, itemIndex, Index(0)), packageType)
              .setValue(NumberOfPackagesPage(hcIndex, itemIndex, Index(0)), quantity)
              .setValue(PackageShippingMarkPage(hcIndex, itemIndex, Index(0)), shippingMark)
              .setValue(PackageTypePage(hcIndex, itemIndex, Index(1)), packageType)
              .setValue(NumberOfPackagesPage(hcIndex, itemIndex, Index(1)), quantity)
              .setValue(PackageShippingMarkPage(hcIndex, itemIndex, Index(1)), shippingMark)
              .setValue(PackageTypePage(hcIndex, itemIndex, Index(2)), packageType)
              .setValue(NumberOfPackagesPage(hcIndex, itemIndex, Index(2)), quantity)
              .setValue(PackageShippingMarkPage(hcIndex, itemIndex, Index(2)), shippingMark)

            val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
            val result = helper.packageSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Packages"
            result.id.value mustEqual "item-1-packages"

            val addOrRemove = result.viewLinks.head
            addOrRemove.id mustEqual "add-remove-item-1-packaging"
            addOrRemove.text mustEqual "Add or remove package"
            addOrRemove.visuallyHidden.value mustEqual "from item 1"
            addOrRemove.href mustEqual controllers.houseConsignment.index.items.packages.routes.AddAnotherPackageController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode, CheckMode)
              .url

            result.children.head mustBe a[AccordionSection]
            result.children.head.sectionTitle.value mustEqual "Package 1"
            result.children.head.id.value mustEqual "item-1-package-1"
            result.children.head.rows.size mustEqual 3

            result.children(1) mustBe a[AccordionSection]
            result.children(1).sectionTitle.value mustEqual "Package 2"
            result.children(1).id.value mustEqual "item-1-package-2"
            result.children(1).rows.size mustEqual 3

            result.children(2) mustBe a[AccordionSection]
            result.children(2).sectionTitle.value mustEqual "Package 3"
            result.children(2).id.value mustEqual "item-1-package-3"
            result.children(2).rows.size mustEqual 3
        }
      }
    }
  }
}
