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
import pages.houseConsignment.index.items._
import pages.houseConsignment.index.items.document.AddDocumentYesNoPage
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageShippingMarkPage, PackageTypePage}

class HouseConsignmentItemNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new HouseConsignmentItemNavigator

  "HouseConsignmentItemNavigator" - {

    "in Check mode" - {

      val mode = CheckMode

      "must go from ItemDescriptionPage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(houseConsignmentIndex, itemIndex), "test")

        navigator
          .nextPage(ItemDescriptionPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
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

      "must go from Change Gross Mass Page to House Consignment Item Declaration Summary Page" - {

        val userAnswers = emptyUserAnswers.setValue(GrossWeightPage(houseConsignmentIndex, itemIndex), BigDecimal(123.45))

        navigator
          .nextPage(GrossWeightPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from NetWeight page to CrossCheck page" in {
        forAll(arbitrary[Double]) {
          netWeight =>
            val userAnswers = emptyUserAnswers.setValue(NetWeightPage(houseConsignmentIndex, itemIndex), netWeight)

            navigator
              .nextPage(NetWeightPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
        }
      }

      "must go from Customs Union and Statistics Code Page to House Consignment Item Declaration Summary Page" - {

        val userAnswers = emptyUserAnswers.setValue(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), "12345")

        navigator
          .nextPage(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from Combined Nomenclature Page to House Consignment Item Declaration Summary Page" - {

        val userAnswers = emptyUserAnswers.setValue(CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex), "23")

        navigator
          .nextPage(CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from CommodityCode page to CrossCheck page" in {
        forAll(nonEmptyString) {
          code =>
            val userAnswers = emptyUserAnswers.setValue(CommodityCodePage(houseConsignmentIndex, itemIndex), code)

            navigator
              .nextPage(CommodityCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
        }
      }
    }

    "in Normal mode" - {
      val mode = NormalMode

      "must go from AddCommodityCodeYesNoPage to CommodityCodePage when answered Yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.houseConsignment.index.items.routes.CommodityCodeController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode))

      }

      "must go from AddCommodityCodeYesNoPage to AddDocumentYesNoPage when answered No" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AddDocumentYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode)
          )

      }

      "must go from CommodityCodePage to AddCombinedNomenclatureCodeYesNoPage " in {

        val userAnswers = emptyUserAnswers

        navigator
          .nextPage(CommodityCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.routes.AddCombinedNomenclatureCodeYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode)
          )

      }

      "must go from AddCombinedNomenclatureCodeYesNoPage to CombinedNomenclatureCodePage when answered Yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCombinedNomenclatureCodeYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddCombinedNomenclatureCodeYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.routes.CombinedNomenclatureCodeController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode)
          )

      }

      "must go from AddCombinedNomenclatureCodeYesNoPage to AddDocumentYesNoPage when answered No" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCombinedNomenclatureCodeYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddCombinedNomenclatureCodeYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AddDocumentYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode)
          )

      }

      "must go from CombinedNomenclatureCodePage to AddDocumentYesNoPage" in {

        val userAnswers = emptyUserAnswers

        navigator
          .nextPage(CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.AddDocumentYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode)
          )

      }

      "must go from AddDocumentYesNoPage to TypePage when answered Yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.TypeController
              .onPageLoad(arrivalId, NormalMode, houseConsignmentIndex, itemIndex, documentIndex)
          )

      }

      "must go from AddDocumentYesNoPage to AddAdditionalReferenceYesNoPage when answered No" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.routes.AddAdditionalReferenceYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode)
          )

      }

    }

  }
}
