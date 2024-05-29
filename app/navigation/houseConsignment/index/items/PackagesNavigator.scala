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

package navigation.houseConsignment.index.items

import controllers.houseConsignment.index.items.packages.routes
import models.{CheckMode, Index, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import pages._
import pages.houseConsignment.index.items.packages._
import play.api.mvc.Call

class PackagesNavigator(houseConsignmentMode: Mode, itemMode: Mode) extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex) =>
      ua =>
        Some(
          routes.AddNumberOfPackagesYesNoController.onPageLoad(
            ua.id,
            houseConsignmentMode,
            itemMode,
            NormalMode,
            houseConsignmentIndex,
            itemIndex,
            packageIndex
          )
        )

    case AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex) =>
      ua => addNumberOfPackagesYesNoNavigation(ua, houseConsignmentIndex, itemIndex, packageIndex, NormalMode)

    case NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex) =>
      ua =>
        Some(
          routes.AddPackageShippingMarkYesNoController.onPageLoad(
            ua.id,
            houseConsignmentIndex,
            itemIndex,
            packageIndex,
            houseConsignmentMode,
            itemMode,
            NormalMode
          )
        )

    case AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex) =>
      ua => addPackageShippingMarkYesNoNavigation(ua, houseConsignmentIndex, itemIndex, packageIndex, NormalMode)

    case PackageShippingMarkPage(houseConsignmentIndex, itemIndex, _) =>
      ua => Some(routes.AddAnotherPackageController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
  }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case PackageTypePage(houseConsignmentIndex, _, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))

    case AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex) =>
      ua => addNumberOfPackagesYesNoNavigation(ua, houseConsignmentIndex, itemIndex, packageIndex, CheckMode)

    case NumberOfPackagesPage(houseConsignmentIndex, _, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))

    case AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex) =>
      ua => addPackageShippingMarkYesNoNavigation(ua, houseConsignmentIndex, itemIndex, packageIndex, CheckMode)

    case PackageShippingMarkPage(houseConsignmentIndex, _, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
  }

  private def addNumberOfPackagesYesNoNavigation(
    ua: UserAnswers,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    packageIndex: Index,
    packageMode: Mode
  ): Option[Call] =
    ua.get(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex)).map {
      case true =>
        routes.NumberOfPackagesController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
      case false =>
        packageMode match {
          case NormalMode =>
            routes.AddPackageShippingMarkYesNoController
              .onPageLoad(ua.id, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
          case CheckMode =>
            controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex)
        }
    }

  private def addPackageShippingMarkYesNoNavigation(
    ua: UserAnswers,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    packageIndex: Index,
    packageMode: Mode
  ): Option[Call] =
    ua.get(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex)).map {
      case true =>
        routes.PackageShippingMarkController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, packageMode)
      case false =>
        packageMode match {
          case NormalMode =>
            routes.AddAnotherPackageController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          case CheckMode =>
            controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex)
        }
    }
}

object PackagesNavigator {

  class PackagesNavigatorProvider {

    def apply(houseConsignmentMode: Mode, itemMode: Mode): PackagesNavigator =
      new PackagesNavigator(houseConsignmentMode, itemMode)
  }
}
