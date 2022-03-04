/*
 * Copyright 2022 HM Revenue & Customs
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

import com.google.inject.{Inject, Singleton}
import controllers.routes
import models.{CheckMode, Mode, NormalMode, UnloadingPermission, UserAnswers}
import pages._
import play.api.mvc.Call

@Singleton
class NavigatorUnloadingPermission @Inject() () {

  private val normalRoutes: Page => UserAnswers => Option[UnloadingPermission] => Call = {
    case DateGoodsUnloadedPage =>
      ua => {
        case Some(unloadingPermission) if unloadingPermission.seals.fold(false)(_.numberOfSeals > 0) =>
          routes.CanSealsBeReadController.onPageLoad(ua.id, NormalMode)
        case _ => routes.UnloadingSummaryController.onPageLoad(ua.id)
      }

    case _ =>
      ua => _ => routes.IndexController.onPageLoad(ua.id)
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, unloadingPermission: Option[UnloadingPermission]): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)(unloadingPermission)
    case CheckMode =>
      routes.CheckYourAnswersController.onPageLoad(userAnswers.id)
  }
}
