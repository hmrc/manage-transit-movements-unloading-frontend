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
import controllers.houseConsignment.index.items.packages.routes
import generators.Generators
import models._
import models.reference.PackageType
import navigation.houseConsignment.index.items.PackagesNavigator.PackagesNavigatorProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.packages._

class PackagesNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigatorProvider = new PackagesNavigatorProvider

  "PackagesNavigator" - {

    "in Normal mode" - {

      val (houseConsignmentMode, itemMode) = arbitrary[(Mode, Mode)]
        .retryUntil {
          case (NormalMode, CheckMode) => false
          case _                       => true
        }
        .sample
        .value

      val packageMode = NormalMode
      val navigator   = navigatorProvider.apply(houseConsignmentMode, itemMode)

      "must go from PackageTypePage to AddNumberOfPackagesYesNoPage" in {

        forAll(arbitrary[PackageType]) {
          packageType =>
            val userAnswers = emptyUserAnswers.setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)

            navigator
              .nextPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
              .mustBe(
                routes.AddNumberOfPackagesYesNoController
                  .onPageLoad(arrivalId, houseConsignmentMode, itemMode, packageMode, houseConsignmentIndex, itemIndex, packageIndex)
              )
        }
      }

      "must go from AddNumberOfPackagesYesNoPage page" - {
        "when user answers Yes to NumberOfPackages page" in {
          val userAnswers = emptyUserAnswers.setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

          navigator
            .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
            .mustBe(
              routes.NumberOfPackagesController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
            )
        }

        "when user answers No to AddPackageShippingMarkYesNo page" in {
          val userAnswers = emptyUserAnswers.setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

          navigator
            .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
            .mustBe(
              routes.AddPackageShippingMarkYesNoController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
            )
        }
      }

      "must go from NumberOfPackages to AddPackageShippingMarkYesNo page" in {
        forAll(arbitrary[BigInt]) {
          quantity =>
            val userAnswers = emptyUserAnswers.setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), quantity)

            navigator
              .nextPage(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
              .mustBe(
                routes.AddPackageShippingMarkYesNoController
                  .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
              )
        }
      }

      "must go from AddPackageShippingMarkYesNo page" - {
        "when user answers Yes to PackageShippingMark page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

          navigator
            .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
            .mustBe(
              routes.PackageShippingMarkController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
            )
        }

        "when user answers No to AddAnotherPackage Page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

          navigator
            .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
            .mustBe(
              routes.AddAnotherPackageController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
            )
        }
      }

      "must go from PackageShippingMark to AddAnotherPackage page" in {
        forAll(nonEmptyString) {
          shippingMark =>
            val userAnswers = emptyUserAnswers.setValue(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), shippingMark)

            navigator
              .nextPage(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
              .mustBe(
                routes.AddAnotherPackageController
                  .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
              )
        }
      }

      "must go from AddNumberOfPackagesYesNoPage to NumberOfPackages page when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

        navigator
          .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.NumberOfPackagesController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
          )
      }

      "must go from AddNumberOfPackagesYesNoPage to AddPackageShippingMarkYesNo page when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

        navigator
          .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.AddPackageShippingMarkYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
          )
      }

      "must go from NumberOfPackagesPage to AddPackageShippingMarkYesNo" in {
        val userAnswers = emptyUserAnswers
          .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), BigInt(123))

        navigator
          .nextPage(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.AddPackageShippingMarkYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
          )
      }

      "must go from AddPackageShippingMarkYesNoPage to Add PackageShippingMark when user answer is yes" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

        navigator
          .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.PackageShippingMarkController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
          )
      }

      "must go from AddPackageShippingMarkYesNoPage to Add PackageShippingMark when user answer is no" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

        navigator
          .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.AddAnotherPackageController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          )
      }

      "must go from PackageShippingMarkPage to Add AddAnotherPackage Page" in {
        val userAnswers = emptyUserAnswers
          .setValue(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), "Shipping Mark")

        navigator
          .nextPage(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.AddAnotherPackageController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          )
      }
    }

    "in Check mode" - {

      val houseConsignmentMode = CheckMode
      val itemMode             = CheckMode
      val packageMode          = CheckMode
      val navigator            = navigatorProvider.apply(houseConsignmentMode, itemMode)

      "must go from PackageTypePage to cross-check page" in {

        forAll(arbitrary[PackageType]) {
          packageType =>
            val userAnswers = emptyUserAnswers.setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)

            navigator
              .nextPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
        }
      }

      "must go from AddNumberOfPackagesYesNoPage page" - {
        "when user answers Yes to NumberOfPackages page" in {
          val userAnswers = emptyUserAnswers.setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

          navigator
            .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
            .mustBe(
              routes.NumberOfPackagesController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
            )
        }

        "when user answers No to cross-check page" in {
          val userAnswers = emptyUserAnswers.setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

          navigator
            .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
            .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
        }
      }

      "must go from NumberOfPackages to cross-check page" in {
        forAll(arbitrary[BigInt]) {
          quantity =>
            val userAnswers = emptyUserAnswers.setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), quantity)

            navigator
              .nextPage(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
        }
      }

      "must go from AddPackageShippingMarkYesNo page" - {
        "when user answers Yes to PackageShippingMark page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

          navigator
            .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
            .mustBe(
              routes.PackageShippingMarkController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
            )
        }

        "when user answers No to cross-check Page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

          navigator
            .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
            .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
        }
      }

      "must go from PackageShippingMark to cross-check page" in {
        forAll(nonEmptyString) {
          shippingMark =>
            val userAnswers = emptyUserAnswers.setValue(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), shippingMark)

            navigator
              .nextPage(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
        }
      }

      "must go from AddNumberOfPackagesYesNoPage to NumberOfPackages page when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

        navigator
          .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.NumberOfPackagesController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
          )
      }

      "must go from AddNumberOfPackagesYesNoPage to cross-check page when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

        navigator
          .nextPage(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from NumberOfPackagesPage to cross-check" in {
        val userAnswers = emptyUserAnswers
          .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), BigInt(123))

        navigator
          .nextPage(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from AddPackageShippingMarkYesNoPage to Add PackageShippingMark when user answer is yes" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

        navigator
          .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.PackageShippingMarkController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
          )
      }

      "must go from AddPackageShippingMarkYesNoPage to cross-check page when user answer is no" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

        navigator
          .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from PackageShippingMarkPage to cross-check page" in {
        val userAnswers = emptyUserAnswers
          .setValue(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), "Shipping Mark")

        navigator
          .nextPage(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), packageMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }
    }
  }
}
