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
import pages.transportEquipment.index._
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.mvc.Call

@Singleton
class TransportEquipmentNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {

    case AddContainerIdentificationNumberYesNoPage(equipmentIndex) => ua => addContainerIdentificationNumberYesNoRoute(ua, equipmentIndex, NormalMode)
    case ContainerIdentificationNumberPage(equipmentIndex) =>
      ua => Some(controllers.transportEquipment.index.routes.AddSealYesNoController.onPageLoad(ua.id, equipmentIndex, NormalMode))
    case AddSealYesNoPage(equipmentIndex) => ua => addSealYesNoRoute(ua, equipmentIndex, NormalMode)
    case SealIdentificationNumberPage(equipmentIndex, _) =>
      ua => Some(controllers.transportEquipment.index.routes.AddAnotherSealController.onPageLoad(ua.id, NormalMode, equipmentIndex))
    case ItemPage(equipmentIndex, _) =>
      ua => Some(controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(ua.id, NormalMode, equipmentIndex))
    case ApplyAnItemYesNoPage(equipmentIndex) => ua => applyAnItemYesNoRoute(ua, ua.id, NormalMode, equipmentIndex)
  }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ContainerIdentificationNumberPage(_) => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case SealIdentificationNumberPage(_, _)   => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case ItemPage(equipmentIndex, _) =>
      ua => Some(controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(ua.id, CheckMode, equipmentIndex))
    case ApplyAnItemYesNoPage(equipmentIndex) => ua => applyAnItemYesNoRoute(ua, ua.id, CheckMode, equipmentIndex)

  }

  private def addContainerIdentificationNumberYesNoRoute(ua: UserAnswers, equipmentIndex: Index, mode: Mode): Option[Call] =
    ua.get(AddContainerIdentificationNumberYesNoPage(equipmentIndex)).map {
      case true  => controllers.transportEquipment.index.routes.ContainerIdentificationNumberController.onPageLoad(ua.id, equipmentIndex, mode)
      case false => controllers.transportEquipment.index.routes.AddSealYesNoController.onPageLoad(ua.id, equipmentIndex, mode)
    }

  private def addSealYesNoRoute(ua: UserAnswers, equipmentIndex: Index, mode: Mode): Option[Call] =
    ua.get(AddSealYesNoPage(equipmentIndex)).map {
      case true =>
        controllers.transportEquipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(ua.id, mode, NormalMode, equipmentIndex, Index(0))
      case false =>
        controllers.transportEquipment.index.routes.ApplyAnItemYesNoController.onPageLoad(ua.id, equipmentIndex, mode)
    }

  def applyAnItemYesNoRoute(ua: UserAnswers, arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index): Option[Call] =
    ua.get(ApplyAnItemYesNoPage(equipmentIndex)).map {
      case true =>
        controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, equipmentIndex, Index(0), mode)
      case false => controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(ua.id, mode)
    }
}
