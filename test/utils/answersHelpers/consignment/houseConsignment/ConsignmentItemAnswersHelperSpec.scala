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

import models.Index
import models.reference.Country
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

              result.key.value mustBe "Description"
              result.value.value mustBe value
              result.actions must not be defined
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
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.grossWeightRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[BigDecimal]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.grossWeightRow.value

              result.key.value mustBe "Gross weight"
              result.value.value mustBe s"${value}kg"
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.visuallyHiddenText.value mustBe "gross weight of item 1"
              action.href mustBe "#"
          }
        }
      }
    }

    "netWeightRow" - {
      val page = NetWeightPage(hcIndex, itemIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.netWeightRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Double]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.netWeightRow.value

              result.key.value mustBe "Net weight"
              result.value.value mustBe s"${value}kg"
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.href mustBe "#"
              action.visuallyHiddenText.value mustBe "net weight of item 1"
              action.id mustBe "change-net-weight-1"
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

      "must return None" - {
        s"when $page undefined" in {

          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          val result = helper.commodityCodeRow
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          val userAnswers = emptyUserAnswers.setValue(page, value)

          val helper = new ConsignmentItemAnswersHelper(userAnswers, hcIndex, itemIndex)
          val result = helper.commodityCodeRow.value

          result mustBe
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

      "must return None" - {
        s"when $page undefined" in {

          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          val result = helper.nomenclatureCodeRow
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          val userAnswers = emptyUserAnswers.setValue(page, value)

          val helper = new ConsignmentItemAnswersHelper(userAnswers, hcIndex, itemIndex)
          val result = helper.nomenclatureCodeRow.value

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
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (referenceNumber, additionalInformation) =>
            val answers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(0)), referenceNumber)
              .setValue(AdditionalInformationPage(hcIndex, itemIndex, Index(0)), additionalInformation)
              .setValue(DocumentReferenceNumberPage(hcIndex, itemIndex, Index(1)), referenceNumber)
              .setValue(AdditionalInformationPage(hcIndex, itemIndex, Index(1)), additionalInformation)

            val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
            val result = helper.documentSections

            result.head mustBe a[AccordionSection]
            result.head.sectionTitle.value mustBe "Document 1"
            result.head.rows.size mustBe 2

            result(1) mustBe a[AccordionSection]
            result(1).sectionTitle.value mustBe "Document 2"
            result(1).rows.size mustBe 2
        }
      }
    }
  }
}
