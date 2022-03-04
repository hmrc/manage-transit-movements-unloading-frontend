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
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.{ConfirmRemoveSealPage, _}
import play.api.mvc.Call

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {

    case UnloadingGuidancePage =>
      ua => routes.DateGoodsUnloadedController.onPageLoad(ua.id, NormalMode)

    case DateGoodsUnloadedPage =>
      ua => routes.CanSealsBeReadController.onPageLoad(ua.id, NormalMode)

    case CanSealsBeReadPage =>
      ua =>
        ua.get(CanSealsBeReadPage) match {
          case Some(_) => routes.AreAnySealsBrokenController.onPageLoad(ua.id, NormalMode)
          case _       => routes.SessionExpiredController.onPageLoad() //TODO temporary redirect will be error page
        }

    case AreAnySealsBrokenPage =>
      ua =>
        ua.get(AreAnySealsBrokenPage) match {
          case Some(_) => routes.UnloadingSummaryController.onPageLoad(ua.id)
          case _       => routes.SessionExpiredController.onPageLoad() //TODO temporary redirect will be error page
        }

    case ChangesToReportPage =>
      ua => routes.UnloadingSummaryController.onPageLoad(ua.id)

    case NewSealNumberPage(_) =>
      ua => routes.UnloadingSummaryController.onPageLoad(ua.id)

    case ConfirmRemoveCommentsPage =>
      ua => routes.UnloadingSummaryController.onPageLoad(ua.id)

    case ConfirmRemoveSealPage =>
      ua => routes.UnloadingSummaryController.onPageLoad(ua.id)

    case _ =>
      ua => routes.IndexController.onPageLoad(ua.id)

  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case VehicleNameRegistrationReferencePage =>
      ua => routes.UnloadingSummaryController.onPageLoad(ua.id)
    case VehicleRegistrationCountryPage =>
      ua => routes.UnloadingSummaryController.onPageLoad(ua.id)
    case GrossMassAmountPage =>
      ua => routes.UnloadingSummaryController.onPageLoad(ua.id)
    case TotalNumberOfItemsPage =>
      ua => routes.UnloadingSummaryController.onPageLoad(ua.id)
    case TotalNumberOfPackagesPage =>
      ua => routes.UnloadingSummaryController.onPageLoad(ua.id)
    case NewSealNumberPage(_) =>
      ua => routes.UnloadingSummaryController.onPageLoad(ua.id)
    case _ =>
      ua => routes.CheckYourAnswersController.onPageLoad(ua.id)
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

}
