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

package navigation

import com.google.inject.Singleton
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode, UserAnswers}
import pages._
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import pages.transportEquipment.index.{AddAnotherSealPage, ApplyAnotherItemPage, ItemPage}
import play.api.mvc.Call

@Singleton
class TransportEquipmentNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddAnotherSealPage(equipmentIndex, sealIndex)   => ua => addAnotherSealRoute(ua, ua.id, NormalMode, equipmentIndex, sealIndex)
    case ApplyAnotherItemPage(equipmentIndex, itemIndex) => ua => applyAnotherItemRoute(ua, ua.id, NormalMode, equipmentIndex, itemIndex)
  }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ContainerIdentificationNumberPage(_)          => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case SealIdentificationNumberPage(_, _)            => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case AddAnotherSealPage(equipmentIndex, sealIndex) => ua => addAnotherSealRoute(ua, ua.id, CheckMode, equipmentIndex, sealIndex)
    case ItemPage(equipmentIndex, _) =>
      ua => Some(controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(ua.id, CheckMode, equipmentIndex))
    case ApplyAnotherItemPage(equipmentIndex, itemIndex) => ua => applyAnotherItemRoute(ua, ua.id, CheckMode, equipmentIndex, itemIndex)
  }

  def addAnotherSealRoute(ua: UserAnswers, arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index, sealIndex: Index): Option[Call] =
    ua.get(AddAnotherSealPage(equipmentIndex, sealIndex)) match {
      case Some(true) =>
        Some(controllers.transportEquipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(arrivalId, mode, equipmentIndex, sealIndex))
      case Some(false) => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  def applyAnotherItemRoute(ua: UserAnswers, arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index, itemIndex: Index): Option[Call] =
    ua.get(ApplyAnotherItemPage(equipmentIndex, itemIndex)) match {
      case Some(true) =>
        Some(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, equipmentIndex, itemIndex, mode))
      case Some(false) => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }
}
