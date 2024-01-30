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

package navigation

import com.google.inject.{Inject, Singleton}
import controllers.routes
import models.{CheckMode, Mode, NormalMode, RichCC043CType, UserAnswers}
import pages._
import play.api.mvc.Call
import utils.Format._

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {

    case UnloadingTypePage => ua => routes.DateGoodsUnloadedController.onPageLoad(ua.id, NormalMode)
    case DateGoodsUnloadedPage =>
      ua =>
        if (ua.ie043Data.sealsExist) {
          controllers.routes.CanSealsBeReadController.onPageLoad(ua.id, NormalMode)
        } else {
          routes.AddUnloadingCommentsYesNoController.onPageLoad(ua.id, NormalMode)
        }

    case CanSealsBeReadPage    => ua => routes.AreAnySealsBrokenController.onPageLoad(ua.id, NormalMode)
    case AreAnySealsBrokenPage => ua => routes.UnloadingFindingsController.onPageLoad(ua.id)
    case UnloadingCommentsPage => ua => routes.CheckYourAnswersController.onPageLoad(ua.id)
    case AddUnloadingCommentsYesNoPage =>
      ua =>
        ua.get(AddUnloadingCommentsYesNoPage)(intToBooleanReads) match {
          case Some(true)  => controllers.routes.UnloadingCommentsController.onPageLoad(ua.id, NormalMode)
          case Some(false) => controllers.routes.CheckYourAnswersController.onPageLoad(ua.id)
          case _           => routes.SessionExpiredController.onPageLoad()
        }

    case _ =>
      ua => routes.SessionExpiredController.onPageLoad()

  }

  private val checkRoutes: Page => UserAnswers => Call = {
    case AddUnloadingCommentsYesNoPage =>
      ua =>
        ua.get(AddUnloadingCommentsYesNoPage)(intToBooleanReads) match {
          case Some(true) =>
            ua.get(UnloadingCommentsPage) match {
              case Some(_) => controllers.routes.CheckYourAnswersController.onPageLoad(ua.id)
              case _       => controllers.routes.UnloadingCommentsController.onPageLoad(ua.id, CheckMode)
            }
          case Some(false) => controllers.routes.CheckYourAnswersController.onPageLoad(ua.id)
          case _           => routes.SessionExpiredController.onPageLoad()
        }
    case _ => ua => routes.CheckYourAnswersController.onPageLoad(ua.id)
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRoutes(page)(userAnswers)
  }

}
