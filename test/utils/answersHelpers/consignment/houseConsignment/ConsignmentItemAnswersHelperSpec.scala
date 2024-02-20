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

import models.reference.PackageType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.NetWeightPage
import pages.houseConsignment.index.items.packaging.{PackagingCountPage, PackagingMarksPage, PackagingTypePage}
import pages.houseConsignment.index.items.{GrossWeightPage, ItemDescriptionPage}
import utils.answersHelpers.AnswersHelperSpecBase

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

    "packageTypeRow" - {
      val page = PackagingTypePage(hcIndex, itemIndex, packageIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.packageTypeRow(packageIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[PackageType]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.packageTypeRow(packageIndex).value

              result.key.value mustBe "Type"
              result.value.value mustBe s"${value.asDescription}"
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.visuallyHiddenText.value mustBe "type of package 1 for item 1"
              action.href mustBe "#"
          }
        }
      }
    }

    "packageCountRow" - {
      val page = PackagingCountPage(hcIndex, itemIndex, packageIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.packageCountRow(packageIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[BigInt]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.packageCountRow(packageIndex).value

              result.key.value mustBe "Quantity"
              result.value.value mustBe s"$value"
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.visuallyHiddenText.value mustBe "quantity of package 1 for item 1"
              action.href mustBe "#"
          }
        }
      }
    }

    "packageMarksRow" - {
      val page = PackagingMarksPage(hcIndex, itemIndex, packageIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ConsignmentItemAnswersHelper(emptyUserAnswers, hcIndex, itemIndex)
          helper.packageMarksRow(packageIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[String]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ConsignmentItemAnswersHelper(answers, hcIndex, itemIndex)
              val result = helper.packageMarksRow(packageIndex).value

              result.key.value mustBe "Shipping mark"
              result.value.value mustBe s"$value"
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.visuallyHiddenText.value mustBe "shipping mark of package 1 for item 1"
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
  }
}
