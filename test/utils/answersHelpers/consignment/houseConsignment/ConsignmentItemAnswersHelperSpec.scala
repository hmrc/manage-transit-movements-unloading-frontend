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
import models.{CheckMode, Index}
import models.reference.Country
import models.reference.DocumentType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.NetWeightPage
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
          helper.descriptionRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.descriptionRow.value
              result.value.value mustBe value
              val actions = result.actions.get.items
              result.key.value mustBe "Description"
              val action = actions.head
              action.href mustBe controllers.houseConsignment.index.items.routes.DescriptionController.onPageLoad(arrivalId, CheckMode, hcIndex, itemIndex).url
              action.visuallyHiddenText.get mustBe "description"

          }
        }
      }
    }

    "declarationTypeRow" - {
      val page = DeclarationTypePage(hcIndex, itemIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.declarationType mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.declarationType.value

              result.key.value mustBe "Declaration type"
              result.value.value mustBe value
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
          helper.countryOfDestination mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.countryOfDestination.value

              result.key.value mustBe "Country of destination"
              result.value.value mustBe value.description
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
          helper.grossWeightRow.key.value mustBe "Gross weight"

          helper.grossWeightRow.value.content.asHtml.toString() must include("Add gross weight")
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[BigDecimal]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.grossWeightRow

              result.key.value mustBe "Gross weight"
              result.value.value mustBe s"${value}kg"
              val action1 = result.actions.value.items.head
              action1.content.value mustBe "Change"
              action1.visuallyHiddenText.value mustBe "gross weight of item 1"
              action1.href mustBe "#"
              val action2 = result.actions.value.items(1)
              action2.content.value mustBe "Remove"
              action2.visuallyHiddenText.value mustBe "gross weight of item 1"
              action2.href mustBe "#"
          }
        }
      }
    }

    "netWeightRow" - {
      val page = NetWeightPage(hcIndex, itemIndex)
      "must return add link" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.netWeightRow.key.value mustBe "Net weight"

          helper.netWeightRow.value.content.asHtml.toString() must include("Add net weight")
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Double]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.netWeightRow

              result.key.value mustBe "Net weight"
              result.value.value mustBe s"${value}kg"
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.routes.NetWeightController.onPageLoad(arrivalId, hcIndex, itemIndex, CheckMode).url
              action.visuallyHiddenText.value mustBe "net weight of item 1"
              action.id mustBe "change-net-weight-1"

              val action2 = result.actions.value.items(1)
              action2.content.value mustBe "Remove"
              action2.visuallyHiddenText.value mustBe "net weight of item 1"
              action2.href mustBe "#"
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
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          val userAnswers = emptyUserAnswers.setValue(page, value)

          val helper = new ConsignmentItemAnswersHelper(userAnswers, hcIndex, itemIndex)
          val result = helper.cusCodeRow.value

          result mustBe
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
          helper.commodityCodeRow.key.value mustBe "Commodity code"

          helper.commodityCodeRow.value.content.asHtml.toString() must include("Add commodity code")
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          val userAnswers = emptyUserAnswers.setValue(page, value)

          val helper = new ConsignmentItemAnswersHelper(userAnswers, hcIndex, itemIndex)
          val result = helper.commodityCodeRow

          val actions = result.actions.get.items
          result.key.value mustBe "Commodity code"
          val action = actions.head
          action.href mustBe controllers.houseConsignment.index.items.routes.CommodityCodeController.onPageLoad(arrivalId, hcIndex, itemIndex, CheckMode).url
          action.visuallyHiddenText.get mustBe s"commodity code for item ${itemIndex.display}"

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
          result.key.value mustBe "Combined nomenclature code"

          result.value.content.asHtml.toString() must include("Add nomenclature code")
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          val userAnswers = emptyUserAnswers.setValue(page, value)

          val helper = new ConsignmentItemAnswersHelper(userAnswers, hcIndex, itemIndex)
          val result = helper.nomenclatureCodeRow

          result mustBe
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
            val result = helper.documentSections

            result.head mustBe a[AccordionSection]
            result.head.sectionTitle.value mustBe "Document 1"
            result.head.rows.size mustBe 3

            result(1) mustBe a[AccordionSection]
            result(1).sectionTitle.value mustBe "Document 2"
            result(1).rows.size mustBe 3

            result(2) mustBe a[AccordionSection]
            result(2).sectionTitle.value mustBe "Document 3"
            result(2).rows.size mustBe 3
        }
      }
    }
  }
}
