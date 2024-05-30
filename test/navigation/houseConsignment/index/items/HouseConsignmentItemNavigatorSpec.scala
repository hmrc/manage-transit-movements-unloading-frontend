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
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator.HouseConsignmentItemNavigatorProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items._
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageShippingMarkPage, PackageTypePage}

class HouseConsignmentItemNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigatorProvider = new HouseConsignmentItemNavigatorProvider

  "HouseConsignmentItemNavigator" - {

    "in Normal mode" - {

      val houseConsignmentMode: Mode = arbitrary[Mode].sample.value
      val itemMode: Mode             = NormalMode
      val navigator                  = navigatorProvider.apply(houseConsignmentMode)

      "must go from ItemDescriptionPage to AddGrossWeightYesNoPage" in {

        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(houseConsignmentIndex, itemIndex), "test")

        navigator
          .nextPage(ItemDescriptionPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.AddGrossWeightYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
      }

      "must go from AddGrossWeightYesNoPage to GrossWeightPage when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.GrossWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
      }

      "must go from AddGrossWeightYesNoPage to AddNetWeightYesNoPage when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.AddNetWeightYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
      }

      "must go from GrossWeightPage to AddNetWeightYesNoPage" in {

        val userAnswers = emptyUserAnswers
          .setValue(GrossWeightPage(houseConsignmentIndex, itemIndex), BigDecimal(123.45))

        navigator
          .nextPage(GrossWeightPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.AddNetWeightYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
      }

      "must go from AddNetWeightYesNoPage to NetWeightPage when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.NetWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
      }

      "must go from AddNetWeightYesNoPage to AddCustomsUnionAndStatisticsCodeYesNoPage when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(
            routes.AddCustomsUnionAndStatisticsCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          )
      }

      "must go from NetWeightPage to AddCustomsUnionAndStatisticsCodeYesNoPage" in {

        val userAnswers = emptyUserAnswers
          .setValue(NetWeightPage(houseConsignmentIndex, itemIndex), BigDecimal(20.351))

        navigator
          .nextPage(NetWeightPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(
            routes.AddCustomsUnionAndStatisticsCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          )
      }

      "must go from AddCustomsUnionAndStatisticsCodeYesNoPage to CustomsUnionAndStatisticsCodePage when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.CustomsUnionAndStatisticsCodeController.onPageLoad(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex))
      }

      "must go from AddCustomsUnionAndStatisticsCodeYesNoPage to AddCommodityCodeYesNoPage when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.AddCommodityCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
      }

      "must go from CustomsUnionAndStatisticsCodePage to AddCommodityCodeYesNoPage" in {

        val userAnswers = emptyUserAnswers
          .setValue(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), "code")

        navigator
          .nextPage(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.AddCommodityCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
      }

      "must go from AddAdditionalReferenceYesNo page" - {
        "when user answers Yes to AdditionalReferenceType page" in {
          val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), true)

          navigator
            .nextPage(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.additionalReference.routes.AdditionalReferenceTypeController
                .onPageLoad(arrivalId, houseConsignmentMode, itemMode, NormalMode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
            )
        }

        "when user answers No to AddPackagesYesNoPage" in {
          val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), false)

          navigator
            .nextPage(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.routes.AddPackagesYesNoController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
            )
        }
      }

      "must go from AddPackagesYesNo page" - {
        "when user answers Yes to PackageType page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), true)

          navigator
            .nextPage(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.packages.routes.PackageTypeController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, NormalMode)
            )
        }

        "when user answers No to AddAnotherItem Page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), false)

          navigator
            .nextPage(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.routes.AddAnotherItemController.onPageLoad(arrivalId, houseConsignmentIndex, houseConsignmentMode)
            )
        }
      }

      "must go from AddCommodityCodeYesNoPage to CommodityCodePage when answered Yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.routes.CommodityCodeController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          )
      }

      "must go from AddCommodityCodeYesNoPage to AddDocumentYesNoPage when answered No" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.AddDocumentYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode))
      }

      "must go from CommodityCodePage to AddCombinedNomenclatureCodeYesNoPage " in {

        val userAnswers = emptyUserAnswers

        navigator
          .nextPage(CommodityCodePage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.routes.AddCombinedNomenclatureCodeYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          )
      }

      "must go from AddCombinedNomenclatureCodeYesNoPage to CombinedNomenclatureCodePage when answered Yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCombinedNomenclatureCodeYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddCombinedNomenclatureCodeYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.routes.CombinedNomenclatureCodeController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          )
      }

      "must go from AddCombinedNomenclatureCodeYesNoPage to AddDocumentYesNoPage when answered No" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddCombinedNomenclatureCodeYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddCombinedNomenclatureCodeYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.AddDocumentYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode))
      }

      "must go from CombinedNomenclatureCodePage to AddDocumentYesNoPage" in {

        val userAnswers = emptyUserAnswers

        navigator
          .nextPage(CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.AddDocumentYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode))
      }

      "must go from AddDocumentYesNoPage to TypePage when answered Yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.TypeController
              .onPageLoad(arrivalId, houseConsignmentMode, itemMode, NormalMode, houseConsignmentIndex, itemIndex, documentIndex)
          )
      }

      "must go from AddDocumentYesNoPage to AddAdditionalReferenceYesNoPage when answered No" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.routes.AddAdditionalReferenceYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          )
      }
    }

    "in Check mode" - {

      val houseConsignmentMode: Mode = CheckMode
      val itemMode: Mode             = CheckMode
      val navigator                  = navigatorProvider.apply(houseConsignmentMode)

      "must go from ItemDescriptionPage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(houseConsignmentIndex, itemIndex), "test")

        navigator
          .nextPage(ItemDescriptionPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from PackageTypePage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), PackageType("code", "description"))

        navigator
          .nextPage(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex), itemMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from PackageShippingMarkPage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), "Shipping mark")

        navigator
          .nextPage(PackageShippingMarkPage(houseConsignmentIndex, itemIndex, packageIndex), itemMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from NumberOfPackagesPage to UnloadingFindingsController" in {

        val userAnswers = emptyUserAnswers
          .setValue(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), BigInt(1))

        navigator
          .nextPage(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), itemMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from Change Gross Mass Page to House Consignment Item Declaration Summary Page" - {

        val userAnswers = emptyUserAnswers.setValue(GrossWeightPage(houseConsignmentIndex, itemIndex), BigDecimal(123.45))

        navigator
          .nextPage(GrossWeightPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from NetWeight page to CrossCheck page" in {
        forAll(arbitrary[BigDecimal]) {
          netWeight =>
            val userAnswers = emptyUserAnswers.setValue(NetWeightPage(houseConsignmentIndex, itemIndex), netWeight)

            navigator
              .nextPage(NetWeightPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex))
        }
      }

      "must go from Customs Union and Statistics Code Page to House Consignment Item Declaration Summary Page" - {

        val userAnswers = emptyUserAnswers.setValue(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), "12345")

        navigator
          .nextPage(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from Combined Nomenclature Page to House Consignment Item Declaration Summary Page" - {

        val userAnswers = emptyUserAnswers.setValue(CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex), "23")

        navigator
          .nextPage(CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from CommodityCode page to HouseConsignment cross check page" in {
        forAll(nonEmptyString) {
          code =>
            val userAnswers = emptyUserAnswers.setValue(CommodityCodePage(houseConsignmentIndex, itemIndex), code)

            navigator
              .nextPage(CommodityCodePage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
              .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
        }
      }

      "must go from AddAdditionalReferenceYesNoPage to HouseConsignmentController page when No selected" in {
        val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(
            controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, hcIndex)
          )
      }

      "must go from AddAdditionalReferenceYesNoPage to AdditionalReferenceTypeController page when Yes selected" in {
        val userAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.additionalReference.routes.AdditionalReferenceTypeController
              .onPageLoad(arrivalId, houseConsignmentMode, itemMode, NormalMode, hcIndex, itemIndex, additionalReferenceIndex)
          )
      }

      "must go from AddDocumentYesNoPage to TypePage when answered Yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(
            controllers.houseConsignment.index.items.document.routes.TypeController
              .onPageLoad(arrivalId, houseConsignmentMode, itemMode, NormalMode, houseConsignmentIndex, itemIndex, documentIndex)
          )
      }

      "must go from AddDocumentYesNoPage to cross-check page when answered No" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from AddPackagesYesNo page" - {
        "when user answers Yes to PackageType page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), true)

          navigator
            .nextPage(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
            .mustBe(
              controllers.houseConsignment.index.items.packages.routes.PackageTypeController
                .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, houseConsignmentMode, itemMode, NormalMode)
            )
        }

        "when user answers No to cross-check Page" in {
          val userAnswers = emptyUserAnswers.setValue(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), false)

          navigator
            .nextPage(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
            .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
        }
      }

      "must go from AddGrossWeightYesNoPage to GrossWeightPage when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.GrossWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
      }

      "must go from AddGrossWeightYesNoPage to cross-check page when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }

      "must go from AddNetWeightYesNoPage to NetWeightPage when answer is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), true)

        navigator
          .nextPage(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(routes.NetWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode))
      }

      "must go from AddNetWeightYesNoPage to cross-check page when answer is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), false)

        navigator
          .nextPage(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex), itemMode, userAnswers)
          .mustBe(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
      }
    }
  }
}
