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
          helper.packageTypeRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[PackageType]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new PackagingAnswersHelper(answers, hcIndex, itemIndex, packageIndex)
              val result = helper.packageTypeRow.value

              result.key.value mustBe "Type"
              result.value.value mustBe s"${value.description}"
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.visuallyHiddenText.value mustBe "type of package 1 for item 1"
              action.href mustBe controllers.houseConsignment.index.items.packages.routes.PackageTypeController
                .onPageLoad(arrivalId, hcIndex, packageIndex, itemIndex, CheckMode, CheckMode, CheckMode)
                .url
              action.id mustBe "change-package-type-1-1"
          }
        }
      }
    }

    "packageCountRow" - {
      val page = NumberOfPackagesPage(hcIndex, itemIndex, packageIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new PackagingAnswersHelper(emptyUserAnswers, hcIndex, itemIndex, packageIndex)
          helper.packageCountRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[BigInt]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new PackagingAnswersHelper(answers, hcIndex, itemIndex, packageIndex)
              val result = helper.packageCountRow.value

              result.key.value mustBe "Quantity"
              result.value.value mustBe s"$value"
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.visuallyHiddenText.value mustBe "quantity of package 1 for item 1"
              action.href mustBe controllers.houseConsignment.index.items.packages.routes.NumberOfPackagesController
                .onPageLoad(arrivalId, hcIndex, itemIndex, packageIndex, CheckMode, CheckMode, CheckMode)
                .url
              action.id mustBe "change-package-count-1-1"
          }
        }
      }
    }

    "packageMarksRow" - {
      val page = PackageShippingMarkPage(hcIndex, itemIndex, packageIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new PackagingAnswersHelper(emptyUserAnswers, hcIndex, itemIndex, packageIndex)
          helper.packageMarksRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new PackagingAnswersHelper(answers, hcIndex, itemIndex, packageIndex)
              val result = helper.packageMarksRow.value

              result.key.value mustBe "Shipping mark"
              result.value.value mustBe s"$value"
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.visuallyHiddenText.value mustBe "shipping mark of package 1 for item 1"
              action.href mustBe controllers.houseConsignment.index.items.packages.routes.PackageShippingMarkController
                .onPageLoad(arrivalId, hcIndex, itemIndex, packageIndex, CheckMode, CheckMode, CheckMode)
                .url
              action.id mustBe "change-package-mark-1-1"
          }
        }
      }
    }

  }
}
