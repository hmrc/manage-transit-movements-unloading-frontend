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
import pages.transportEquipment.index._
import play.api.mvc.Call

@Singleton
class TransportEquipmentNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddAnotherSealPage(equipmentIndex, sealIndex) => ua => addAnotherSealRoute(ua, ua.id, NormalMode, equipmentIndex, sealIndex)

    case AddContainerIdentificationNumberYesNoPage(equipmentIndex) => ua => addContainerIdentificationNumberYesNoRoute(ua, equipmentIndex, NormalMode)
    case ContainerIdentificationNumberPage(equipmentIndex) =>
      ua => Some(controllers.transportEquipment.index.routes.AddSealYesNoController.onPageLoad(ua.id, equipmentIndex, NormalMode))
    case AddSealYesNoPage(equipmentIndex) => ua => addSealYesNoRoute(ua, equipmentIndex, NormalMode)
    case SealIdentificationNumberPage(equipmentIndex, _) =>
      ua => Some(controllers.transportEquipment.index.routes.AddAnotherSealController.onPageLoad(ua.id, NormalMode, equipmentIndex))
    case ItemPage(equipmentIndex, _) =>
      ua => Some(controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(ua.id, NormalMode, equipmentIndex))
    case ApplyAnotherItemPage(equipmentIndex, itemIndex) => ua => applyAnotherItemRoute(ua, equipmentIndex, itemIndex, NormalMode)
  }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ContainerIdentificationNumberPage(_)          => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case SealIdentificationNumberPage(_, _)            => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case AddAnotherSealPage(equipmentIndex, sealIndex) => ua => addAnotherSealRoute(ua, ua.id, CheckMode, equipmentIndex, sealIndex)
  }

  def addAnotherSealRoute(ua: UserAnswers, arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index, sealIndex: Index): Option[Call] =
    ua.get(AddAnotherSealPage(equipmentIndex, sealIndex)) match {
      case Some(true) =>
        Some(controllers.transportEquipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(arrivalId, mode, equipmentIndex, sealIndex))
      case Some(false) => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  def addContainerIdentificationNumberYesNoRoute(ua: UserAnswers, equipmentIndex: Index, mode: Mode): Option[Call] =
    ua.get(AddContainerIdentificationNumberYesNoPage(equipmentIndex)) map {
      case true  => controllers.transportEquipment.index.routes.ContainerIdentificationNumberController.onPageLoad(ua.id, equipmentIndex, mode)
      case false => controllers.transportEquipment.index.routes.AddSealYesNoController.onPageLoad(ua.id, equipmentIndex, mode)
    }

  def addSealYesNoRoute(ua: UserAnswers, equipmentIndex: Index, mode: Mode): Option[Call] =
    ua.get(AddSealYesNoPage(equipmentIndex)) match {
      case Some(true) =>
        Some(controllers.transportEquipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(ua.id, mode, equipmentIndex, Index(0)))
      case Some(false) =>
        Some(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(ua.id, equipmentIndex, Index(0), mode))
      case _ =>
        Some(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(ua.id, equipmentIndex, Index(0), mode))
    }

  def applyAnotherItemRoute(ua: UserAnswers, equipmentIndex: Index, itemIndex: Index, mode: Mode): Option[Call] =
    ua.get(ApplyAnotherItemPage(equipmentIndex, itemIndex)) match {
      case Some(true) =>
        Some(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(ua.id, equipmentIndex, Index(0), mode))
      case Some(false) =>
        Some(controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(ua.id, mode))
      case _ =>
        Some(controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(ua.id, mode))
    }

}
