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

package navigation.houseConsignment.index.items

import base.SpecBase
import generators.Generators
import models._
import models.reference.PackageType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.packages._

class PackagesNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new PackagesNavigator

  "PackagesNavigator" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from PackageTypePage to AddNumberOfPackagesYesNoPage" in {

        forAll(arbitrary[PackageType]) {
          packageType =>
            val userAnswers = emptyUserAnswers.setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)

            navigator
              .nextPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.items.packages.routes.AddNumberOfPackagesYesNoController
                  .onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex, packageIndex)
              )
        }
      }

      "must go from AddNumberOfPackagesYesNoPage page" - {
        "when user answers Yes to NumberOfPackages page" in {
          val userAnswers = emptyUserAnswers.setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

          navigator
            .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.packages.routes.NumberOfPackagesController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
            )
        }

        "when user answers No to AddPackageShippingMarkYesNo page" in {
          val userAnswers = emptyUserAnswers.setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

          navigator
            .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.packages.routes.AddPackageShippingMarkYesNoController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
            )
        }
      }

      "must go from NumberOfPackages to AddPackageShippingMarkYesNo page" in {
        forAll(arbitrary[BigInt]) {
          quantity =>
            val userAnswers = emptyUserAnswers.setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), quantity)

            navigator
              .nextPage(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.items.packages.routes.AddPackageShippingMarkYesNoController
                  .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
              )
        }
      }

      "must go from AddPackageShippingMarkYesNo page" - {
        "when user answers Yes to PackageShippingMark page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

          navigator
            .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.packages.routes.PackageShippingMarkController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
            )
        }

        "when user answers No to AddAnotherPackage Page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

          navigator
            .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.packages.routes.AddAnotherPackageController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
            )
        }
      }

      "must go from PackageShippingMark to AddAnotherPackage page" in {
        forAll(nonEmptyString) {
          shippingMark =>
            val userAnswers = emptyUserAnswers.setValue(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), shippingMark)

            navigator
              .nextPage(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
              .mustBe(
                controllers.houseConsignment.index.items.packages.routes.AddAnotherPackageController
                  .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
              )
        }
      }
    }
  }
}
