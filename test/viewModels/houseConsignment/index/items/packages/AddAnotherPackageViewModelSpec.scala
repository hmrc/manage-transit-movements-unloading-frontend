/*
 * Copyright 2023 HM Revenue & Customs
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

package viewModels.houseConsignment.index.items.packages

import base.SpecBase
import generators.Generators
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.packages.PackageTypePage
import viewModels.houseConsignment.index.items.packages.AddAnotherPackageViewModel.AddAnotherPackageViewModelProvider

class AddAnotherPackageViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is no packages" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val result = new AddAnotherPackageViewModelProvider().apply(emptyUserAnswers, arrivalId, houseConsignmentIndex, itemIndex, mode, mode)

          result.listItems mustBe Nil

          result.title(houseConsignmentIndex, itemIndex) mustBe s"You have added 0 types of packages for item 1 in house consignment 1"
          result.heading(houseConsignmentIndex, itemIndex) mustBe s"You have added 0 types of packages for item 1 in house consignment 1"
          result.legend(houseConsignmentIndex, itemIndex) mustBe s"Do you want to add a type of package for item 1 in house consignment 1?"
          result.maxLimitLabel(houseConsignmentIndex, itemIndex) mustBe
            s"You cannot add any more types of packages for item 1 in house consignment 1. To add another, you need to remove one first."
      }
    }

    "when there is one package" in {
      forAll(arbitrary[Mode], arbitraryPackageType.arbitrary.sample.value) {
        (mode, packageType) =>
          val userAnswers = emptyUserAnswers
            .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)

          val result = new AddAnotherPackageViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, itemIndex, mode, mode)

          result.listItems.length mustBe 1
          result.title(houseConsignmentIndex, itemIndex) mustBe s"You have added 1 type of package for item 1 in house consignment 1"
          result.heading(houseConsignmentIndex, itemIndex) mustBe s"You have added 1 type of package for item 1 in house consignment 1"
          result.legend(houseConsignmentIndex, itemIndex) mustBe s"Do you want to add another type of package for item 1 in house consignment 1?"
          result.maxLimitLabel(houseConsignmentIndex, itemIndex) mustBe
            s"You cannot add any more types of packages for item 1 in house consignment 1. To add another, you need to remove one first."
      }
    }

    "when there are multiple additional references" in {

      forAll(arbitrary[Mode], arbitraryPackageType.arbitrary.sample.value) {
        (mode, packageType) =>
          val userAnswers = emptyUserAnswers
            .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, Index(0)), packageType)
            .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, Index(1)), packageType)
            .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, Index(2)), packageType)
            .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, Index(3)), packageType)

          val result = new AddAnotherPackageViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, itemIndex, mode, mode)
          result.listItems.length mustBe 4
          result.title(houseConsignmentIndex, itemIndex) mustBe s"You have added 4 types of packages for item 1 in house consignment 1"
          result.heading(houseConsignmentIndex, itemIndex) mustBe s"You have added 4 types of packages for item 1 in house consignment 1"
          result.legend(houseConsignmentIndex, itemIndex) mustBe s"Do you want to add another type of package for item 1 in house consignment 1?"
          result.maxLimitLabel(houseConsignmentIndex, itemIndex) mustBe
            s"You cannot add any more types of packages for item 1 in house consignment 1. To add another, you need to remove one first."
      }
    }
  }
}
