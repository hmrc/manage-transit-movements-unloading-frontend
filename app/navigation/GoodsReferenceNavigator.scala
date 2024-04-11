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

import controllers.transportEquipment.index.routes
import models.{Mode, NormalMode, UserAnswers}
import pages._
import pages.transportEquipment.index.ItemPage
import play.api.mvc.Call

class GoodsReferenceNavigator(equipmentMode: Mode) extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ItemPage(equipmentIndex, _) =>
      ua => Some(routes.ApplyAnotherItemController.onPageLoad(ua.id, equipmentMode, NormalMode, equipmentIndex))
  }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ItemPage(_, _) =>
      ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
  }
}

object GoodsReferenceNavigator {

  class GoodsReferenceNavigatorProvider {
    def apply(equipmentMode: Mode): GoodsReferenceNavigator = new GoodsReferenceNavigator(equipmentMode)
  }
}
