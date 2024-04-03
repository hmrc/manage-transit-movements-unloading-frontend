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
import navigation.Navigator
import pages._
import pages.houseConsignment.index.items._
import pages.houseConsignment.index.items.document.AddDocumentYesNoPage
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageShippingMarkPage, PackageTypePage}
import pages.sections.houseConsignment.index.items.documents.DocumentsSection
import play.api.mvc.Call

@Singleton
class HouseConsignmentItemNavigator extends Navigator {

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

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
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
    case _ => _ => Some(Call("GET", "#")) //TODO: Update document navigation
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
