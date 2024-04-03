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
import models.{CheckMode, Index, Mode, NormalMode, UserAnswers}
import controllers.houseConsignment.index.items.routes
import models.{ArrivalId, Index, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import pages._
import pages.houseConsignment.index.items._
import pages.houseConsignment.index.items.document.AddDocumentYesNoPage
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageShippingMarkPage, PackageTypePage}
import pages.sections.houseConsignment.index.items.documents.DocumentsSection
import play.api.mvc.Call

@Singleton
class HouseConsignmentItemNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ItemDescriptionPage(houseConsignmentIndex, itemIndex) =>
      ua => Some(routes.AddGrossWeightYesNoController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, NormalMode))
    case AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex) => ua => addGrossWeightYesNoRoute(ua, ua.id, houseConsignmentIndex, itemIndex, NormalMode)
    case GrossWeightPage(houseConsignmentIndex, itemIndex) =>
      ua => Some(routes.AddNetWeightYesNoController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, NormalMode))
    case AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex) => ua => addNetWeightYesNoRoute(ua, ua.id, houseConsignmentIndex, itemIndex, NormalMode)
    case NetWeightPage(houseConsignment, itemIndex) =>
      ua => Some(routes.AddCustomsUnionAndStatisticsCodeYesNoController.onPageLoad(ua.id, houseConsignment, itemIndex, NormalMode))
    case AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex) =>
      ua => addCustomsUnionAndStatisticsCodeYesNoRoute(ua, ua.id, houseConsignmentIndex, itemIndex, NormalMode)
    case CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex) =>
      ua => Some(routes.AddCommodityCodeYesNoController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, NormalMode))
    case AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex)      => ua => addAdditionalReferenceYesNoRoute(ua, houseConsignmentIndex, itemIndex)
    case AddPackagesYesNoPage(houseConsignmentIndex, itemIndex)                 => ua => addPackagesYesNoRoute(ua, houseConsignmentIndex, itemIndex)
    case AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex)            => ua => addCommodityCodeNav(ua, houseConsignmentIndex, itemIndex)
    case AddCombinedNomenclatureCodeYesNoPage(houseConsignmentIndex, itemIndex) => ua => addCombinedNomenclatureCodeNav(ua, houseConsignmentIndex, itemIndex)
    case AddDocumentYesNoPage(houseConsignmentIndex, itemIndex)                 => ua => addDocumentYesNoNav(ua, houseConsignmentIndex, itemIndex)
    case CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex) =>
      ua =>
        Some(
          controllers.houseConsignment.index.items.document.routes.AddDocumentYesNoController
            .onPageLoad(ua.id, houseConsignmentIndex, itemIndex, NormalMode)
        )
    case CommodityCodePage(houseConsignmentIndex, itemIndex) =>
      ua =>
        Some(
          controllers.houseConsignment.index.items.routes.AddCombinedNomenclatureCodeYesNoController
            .onPageLoad(ua.id, houseConsignmentIndex, itemIndex, NormalMode)
        )
  }

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ItemDescriptionPage(houseConsignmentIndex, _) => ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case PackageTypePage(houseConsignmentIndex, _, _)  => ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case PackageShippingMarkPage(houseConsignmentIndex, _, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case NumberOfPackagesPage(houseConsignmentIndex, _, _) => ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case GrossWeightPage(houseConsignmentIndex, _)         => ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case NetWeightPage(houseConsignmentIndex, _)           => ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case CombinedNomenclatureCodePage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case CommodityCodePage(houseConsignmentIndex, _) => ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, _) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
  }

  def addGrossWeightYesNoRoute(ua: UserAnswers, arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): Option[Call] =
    ua.get(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex)).map {
      case true  => routes.GrossWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
      case false => routes.AddNetWeightYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
    }

  def addNetWeightYesNoRoute(ua: UserAnswers, arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): Option[Call] =
    ua.get(AddNetWeightYesNoPage(houseConsignmentIndex, itemIndex)).map {
      case true  => routes.NetWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
      case false => routes.AddCustomsUnionAndStatisticsCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
    }

  def addCustomsUnionAndStatisticsCodeYesNoRoute(ua: UserAnswers,
                                                 arrivalId: ArrivalId,
                                                 houseConsignmentIndex: Index,
                                                 itemIndex: Index,
                                                 mode: Mode
  ): Option[Call] =
    ua.get(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex)).map {
      case true  => routes.CustomsUnionAndStatisticsCodeController.onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex)
      case false => routes.AddCommodityCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode)
    }

  private def addAdditionalReferenceYesNoRoute(ua: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index): Option[Call] =
    ua.get(AddAdditionalReferenceYesNoPage(houseConsignmentIndex, itemIndex)) map {
      case true =>
        controllers.houseConsignment.index.items.additionalReference.routes.AdditionalReferenceTypeController
          .onPageLoad(ua.id, NormalMode, houseConsignmentIndex, itemIndex, Index(0))
      case false =>
        controllers.houseConsignment.index.items.routes.AddPackagesYesNoController.onPageLoad(ua.id, houseConsignmentIndex, itemIndex, NormalMode)
    }

  private def addPackagesYesNoRoute(ua: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index): Option[Call] =
    ua.get(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex)) map {
      case true =>
        controllers.houseConsignment.index.items.packages.routes.PackageTypeController
          .onPageLoad(ua.id, houseConsignmentIndex, itemIndex, Index(0), NormalMode)
      case false =>
        controllers.houseConsignment.index.items.routes.AddAnotherItemController.onPageLoad(ua.id, houseConsignmentIndex, NormalMode)
    }

  def addCommodityCodeNav(ua: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index): Option[Call] =
    ua.get(AddCommodityCodeYesNoPage(houseConsignmentIndex, itemIndex)).map {
      case true =>
        controllers.houseConsignment.index.items.routes.CommodityCodeController
          .onPageLoad(ua.id, houseConsignmentIndex, itemIndex, CheckMode)
      case false =>
        controllers.houseConsignment.index.items.document.routes.AddDocumentYesNoController
          .onPageLoad(ua.id, houseConsignmentIndex, itemIndex, NormalMode)
    }

  def addCombinedNomenclatureCodeNav(ua: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index): Option[Call] =
    ua.get(AddCombinedNomenclatureCodeYesNoPage(houseConsignmentIndex, itemIndex)).map {
      case true =>
        controllers.houseConsignment.index.items.routes.CombinedNomenclatureCodeController
          .onPageLoad(ua.id, houseConsignmentIndex, itemIndex, CheckMode)
      case false =>
        controllers.houseConsignment.index.items.document.routes.AddDocumentYesNoController
          .onPageLoad(ua.id, houseConsignmentIndex, itemIndex, NormalMode)
    }

  def addDocumentYesNoNav(ua: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index): Option[Call] = {

    val documentIndex = ua.get(DocumentsSection(houseConsignmentIndex, itemIndex)).map(_.value.length).getOrElse(0)

    ua.get(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex)).map {
      case true =>
        controllers.houseConsignment.index.items.document.routes.TypeController
          .onPageLoad(ua.id, NormalMode, houseConsignmentIndex, itemIndex, Index(documentIndex))
      case false =>
        controllers.houseConsignment.index.items.routes.AddAdditionalReferenceYesNoController
          .onPageLoad(ua.id, houseConsignmentIndex, itemIndex, NormalMode)
    }
  }
}
