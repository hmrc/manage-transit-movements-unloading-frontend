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
import controllers.houseConsignment.index.items.routes
import generators.Generators
import models._
import models.reference.PackageType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items._
import pages.houseConsignment.index.items.document.AddDocumentYesNoPage
import pages.houseConsignment.index.items.packages.{AddPackageShippingMarkYesNoPage, NumberOfPackagesPage, PackageShippingMarkPage, PackageTypePage}

class HouseConsignmentItemNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new HouseConsignmentItemNavigator

  "HouseConsignmentItemNavigator" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from ItemDescriptionPage to AddGrossWeightYesNoPage" in {

        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(houseConsignmentIndex, itemIndex), "test")

        navigator
          .nextPage(ItemDescriptionPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AddGrossWeightYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode))
      }

      "must go from AddGrossWeightYesNoPage to GrossWeightPage when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.GrossWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode))
      }

      "must go from AddGrossWeightYesNoPage to AddNetWeightYesNoPage when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AddNetWeightYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode))
      }

      "must go from GrossWeightPage to AddNetWeightYesNoPage" in {

        val userAnswers = emptyUserAnswers
          .setValue(GrossWeightPage(houseConsignmentIndex, itemIndex), BigDecimal(123.45))

        navigator
          .nextPage(GrossWeightPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AddNetWeightYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode))
      }

      "must go from PackageTypePage to AddNumberOfPackagesYesNoPage" in {
        val packageType = PackageType("1A", "Drum, steel")

        val userAnswers = emptyUserAnswers
          .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), packageType)

        navigator
          .nextPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.AddNumberOfPackagesYesNoController
              .onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex, packageIndex)
          )
      }

      "must go from NumberOfPackagesPage to AddPackageShippingMarkYesNo" in {
        val userAnswers = emptyUserAnswers
          .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), BigInt(123))

        navigator
          .nextPage(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.AddPackageShippingMarkYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
          )
      }

      "must go from AddPackageShippingMarkYesNoPage to Add PackageShippingMark when user answer is yes" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), true)

        navigator
          .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.PackageShippingMarkController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
          )
      }

      "must go from AddPackageShippingMarkYesNoPage to Add PackageShippingMark when user answer is no" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), false)

        navigator
          .nextPage(AddPackageShippingMarkYesNoPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.AddAnotherPackageController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
          )
      }

      "must go from PackageShippingMarkPage to Add AddAnotherPackage Page" in {
        val userAnswers = emptyUserAnswers
          .setValue(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), "Shipping Mark")

        navigator
          .nextPage(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.packages.routes.AddAnotherPackageController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
          )
      }

      "must go from AddNetWeightYesNoPage to NetWeightPage when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.NetWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode))
      }

      "must go from AddNetWeightYesNoPage to AddCustomsUnionAndStatisticsCodeYesNoPage when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AddCustomsUnionAndStatisticsCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode))
      }

      "must go from NetWeightPage to AddCustomsUnionAndStatisticsCodeYesNoPage" in {

        val userAnswers = emptyUserAnswers
          .setValue(NetWeightPage(houseConsignmentIndex, itemIndex), 20.351)

        navigator
          .nextPage(NetWeightPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AddCustomsUnionAndStatisticsCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode))
      }

      "must go from AddCustomsUnionAndStatisticsCodeYesNoPage to CustomsUnionAndStatisticsCodePage when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.CustomsUnionAndStatisticsCodeController.onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex))
      }

      "must go from AddCustomsUnionAndStatisticsCodeYesNoPage to AddCommodityCodeYesNoPage when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AddCommodityCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode))
      }

      "must go from CustomsUnionAndStatisticsCodePage to AddCommodityCodeYesNoPage" in {

        val userAnswers = emptyUserAnswers
          .setValue(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), "code")

        navigator
          .nextPage(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(routes.AddCommodityCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode))
      }

      "must go from AddAdditionalReferenceYesNo page" - {
        "when user answers Yes to AdditionalReferenceType page" in {
          val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), true)

          navigator
            .nextPage(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.additionalReference.routes.AdditionalReferenceTypeController
                .onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
            )
        }

        "when user answers No to AddPackagesYesNoPage" in {
          val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), false)

          navigator
            .nextPage(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
            .mustBe(controllers.houseConsignment.index.items.routes.AddPackagesYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode))
        }
      }

      "must go from AddPackagesYesNo page" - {
        "when user answers Yes to PackageType page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), true)

          navigator
            .nextPage(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.packages.routes.PackageTypeController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
            )
        }

        "when user answers No to AddAnotherItem Page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), false)

          navigator
            .nextPage(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.routes.AddAnotherItemController.onPageLoad(arrivalId, houseConsignmentIndex, mode)
            )
        }
      }
    }

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
              .mustBe(
                controllers.houseConsignment.index.items.routes.AddCombinedNomenclatureCodeYesNoController.onPageLoad(arrivalId, hcIndex, itemIndex, NormalMode)
              )
        }
      }

      "must go from AddAdditionalReferenceYesNoPage to HouseConsignmentController page when No selected" in {
        val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(
            controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex)
          )
      }

      "must go from AddAdditionalReferenceYesNoPage to AdditionalReferenceTypeController page when Yes selected" in {
        val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.additionalReference.routes.AdditionalReferenceTypeController
              .onPageLoad(arrivalId, mode, hcIndex, itemIndex, additionalReferenceIndex)
          )
      }
    }

    "in Normal mode" - {
      val mode = NormalMode

      "must go from AddCommodityCodeYesNoPage to CommodityCodePage when answered Yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex), mode, userAnswers)
          .mustBe(controllers.houseConsignment.index.items.routes.CommodityCodeController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode))

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
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode)
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
