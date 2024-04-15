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

import com.google.inject.Singleton
import models.{ArrivalId, Index, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import pages._
import pages.houseConsignment.index.items.packages.{AddNumberOfPackagesYesNoPage, PackageTypePage}
import play.api.mvc.Call

@Singleton
class AddPackagesNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {

    case AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex) =>
      ua => addNumberOfPackagesYesNoRoute(ua, ua.id, houseConsignmentIndex, itemIndex, packageIndex, NormalMode)
  }

  def addNumberOfPackagesYesNoRoute(ua: UserAnswers,
                                    arrivalId: ArrivalId,
                                    houseConsignmentIndex: Index,
                                    itemIndex: Index,
                                    packageIndex: Index,
                                    mode: Mode
  ): Option[Call] =
    ua.get(AddNumberOfPackagesYesNoPage(houseConsignmentIndex, itemIndex, packageIndex)).map {
      case true =>
        controllers.houseConsignment.index.items.packages.routes.NumberOfPackagesController
          .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)

      case false =>
        controllers.houseConsignment.index.items.packages.routes.AddPackageShippingMarkYesNoController
          .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)

    }

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex) =>
      ua =>
        Some(
          controllers.houseConsignment.index.items.packages.routes.AddNumberOfPackagesYesNoController
            .onPageLoad(ua.id, NormalMode, houseConsignmentIndex, itemIndex, packageIndex)
        )
  }

}
