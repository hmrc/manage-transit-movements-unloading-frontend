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

package utils.answersHelpers.consignment.houseConsignment.item

import models.CheckMode
import models.reference.PackageType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageShippingMarkPage, PackageTypePage}
import utils.answersHelpers.AnswersHelperSpecBase

class PackagingAnswersHelperSpec extends AnswersHelperSpecBase {

  "PackagingAnswersHelper" - {

    "packageTypeRow" - {
      val page = PackageTypePage(hcIndex, itemIndex, packageIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new PackagingAnswersHelper(emptyUserAnswers, hcIndex, itemIndex, packageIndex)
          helper.packageTypeRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[PackageType]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new PackagingAnswersHelper(answers, hcIndex, itemIndex, packageIndex)
              val result = helper.packageTypeRow.value

              result.key.value mustEqual "Type"
              result.value.value mustEqual s"${value.description}"
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.visuallyHiddenText.value mustEqual "type of package 1 for item 1"
              action.href mustEqual controllers.houseConsignment.index.items.packages.routes.PackageTypeController
                .onPageLoad(arrivalId, hcIndex, packageIndex, itemIndex, CheckMode, CheckMode, CheckMode)
                .url
              action.id mustEqual "change-package-type-1-1"
          }
        }
      }
    }

    "packageCountRow" - {
      val page = NumberOfPackagesPage(hcIndex, itemIndex, packageIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new PackagingAnswersHelper(emptyUserAnswers, hcIndex, itemIndex, packageIndex)
          helper.packageCountRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[BigInt]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new PackagingAnswersHelper(answers, hcIndex, itemIndex, packageIndex)
              val result = helper.packageCountRow.value

              result.key.value mustEqual "Quantity"
              result.value.value mustEqual s"$value"
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.visuallyHiddenText.value mustEqual "quantity of package 1 for item 1"
              action.href mustEqual controllers.houseConsignment.index.items.packages.routes.NumberOfPackagesController
                .onPageLoad(arrivalId, hcIndex, itemIndex, packageIndex, CheckMode, CheckMode, CheckMode)
                .url
              action.id mustEqual "change-package-count-1-1"
          }
        }
      }
    }

    "packageMarksRow" - {
      val page = PackageShippingMarkPage(hcIndex, itemIndex, packageIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new PackagingAnswersHelper(emptyUserAnswers, hcIndex, itemIndex, packageIndex)
          helper.packageMarksRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new PackagingAnswersHelper(answers, hcIndex, itemIndex, packageIndex)
              val result = helper.packageMarksRow.value

              result.key.value mustEqual "Shipping mark"
              result.value.value mustEqual s"$value"
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.visuallyHiddenText.value mustEqual "shipping mark of package 1 for item 1"
              action.href mustEqual controllers.houseConsignment.index.items.packages.routes.PackageShippingMarkController
                .onPageLoad(arrivalId, hcIndex, itemIndex, packageIndex, CheckMode, CheckMode, CheckMode)
                .url
              action.id mustEqual "change-package-mark-1-1"
          }
        }
      }
    }

  }
}
