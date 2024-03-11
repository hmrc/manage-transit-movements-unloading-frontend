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
import models.{CheckMode, Index, Mode, UserAnswers}
import pages._
import pages.transportEquipment.index.{AddContainerIdentificationNumberYesNoPage, AddSealYesNoPage}
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.mvc.Call

@Singleton
class TransportEquipmentNavigator extends Navigator {

  override def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case _ => _ => Some(Call("GET", "#")) //TODO: Implement nav for normalRoutes
  }

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ContainerIdentificationNumberPage(_) => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case SealIdentificationNumberPage(_, _)   => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
  }
}
