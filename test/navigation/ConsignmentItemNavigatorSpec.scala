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

package navigation

import base.SpecBase
import generators.Generators
import models._
import models.reference.PackageType
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.{CommodityCodePage, ItemDescriptionPage, NetWeightPage}
import pages.{NumberOfPackagesPage, PackageShippingMarkPage, PackageTypePage}

class ConsignmentItemNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new ConsignmentItemNavigator

  "ConsignmentItemNavigator" - {

    "in Check mode" - {

      val mode = CheckMode

      "must go from CommodityCodePage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(CommodityCodePage(houseConsignmentIndex, itemIndex), "Commodity code")

        navigator
          .nextPage(CommodityCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))

      }

      "must go from ItemDescriptionPage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(houseConsignmentIndex, itemIndex), "test")

        navigator
          .nextPage(ItemDescriptionPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))

      }

      "must go from NetWeightPage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(NetWeightPage(houseConsignmentIndex, itemIndex), 1.111)

        navigator
          .nextPage(NetWeightPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))

      }

      "must go from PackageTypePage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), PackageType("code", "description"))

        navigator
          .nextPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))

      }

      "must go from PackageShippingMarkPage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), "Shipping mark")

        navigator
          .nextPage(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))

      }

      "must go from NumberOfPackagesPage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), BigInt(1))

        navigator
          .nextPage(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))

      }

    }
  }
}
