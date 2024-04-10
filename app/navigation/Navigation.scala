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
import controllers.routes
import models.{CheckMode, NormalMode, RichCC043CType, UserAnswers}
import pages._
import play.api.mvc.Call

@Singleton
class Navigation extends Navigator {

  override def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {

    case UnloadingTypePage => ua => Some(routes.DateGoodsUnloadedController.onPageLoad(ua.id, NormalMode))
    case DateGoodsUnloadedPage =>
      ua =>
        if (ua.ie043Data.sealsExist) {
          Some(controllers.routes.CanSealsBeReadController.onPageLoad(ua.id, NormalMode))
        } else {
          Some(routes.UnloadingFindingsController.onPageLoad(ua.id, NormalMode))
        }

    case CanSealsBeReadPage    => ua => Some(routes.AreAnySealsBrokenController.onPageLoad(ua.id, NormalMode))
    case AreAnySealsBrokenPage => ua => Some(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(ua.id, NormalMode))
    case AddTransitUnloadingPermissionDiscrepanciesYesNoPage =>
      ua =>
        ua.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) map {
          case true  => controllers.routes.UnloadingFindingsController.onPageLoad(ua.id, NormalMode)
          case false => controllers.routes.AddCommentsYesNoController.onPageLoad(ua.id, NormalMode)
        }
    case AddCommentsYesNoPage =>
      ua =>
        ua.get(AddCommentsYesNoPage) map {
          case true  => controllers.routes.UnloadingCommentsController.onPageLoad(ua.id, NormalMode)
          case false => controllers.routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(ua.id, NormalMode)
        }
    case UnloadingCommentsPage =>
      ua => Some(routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(ua.id, NormalMode))
    case DoYouHaveAnythingElseToReportYesNoPage =>
      ua =>
        ua.get(DoYouHaveAnythingElseToReportYesNoPage) map {
          case true  => controllers.routes.OtherThingsToReportController.onPageLoad(ua.id, NormalMode)
          case false => controllers.routes.CheckYourAnswersController.onPageLoad(ua.id)
        }
    case OtherThingsToReportPage => ua => Some(controllers.routes.CheckYourAnswersController.onPageLoad(ua.id))
    case _ =>
      _ => Some(routes.SessionExpiredController.onPageLoad())

  }

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case UnloadingTypePage     => ua => Some(controllers.routes.CheckYourAnswersController.onPageLoad(ua.id))
    case DateGoodsUnloadedPage => ua => Some(controllers.routes.CheckYourAnswersController.onPageLoad(ua.id))
    case CanSealsBeReadPage    => ua => Some(controllers.routes.CheckYourAnswersController.onPageLoad(ua.id))
    case AreAnySealsBrokenPage => ua => Some(controllers.routes.CheckYourAnswersController.onPageLoad(ua.id))
    case AddTransitUnloadingPermissionDiscrepanciesYesNoPage =>
      ua =>
        ua.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) map {
          case true  => controllers.routes.UnloadingFindingsController.onPageLoad(ua.id, CheckMode)
          case false => controllers.routes.CheckYourAnswersController.onPageLoad(ua.id)
        }
    case AddCommentsYesNoPage =>
      ua =>
        ua.get(AddCommentsYesNoPage) map {
          case true  => controllers.routes.UnloadingCommentsController.onPageLoad(ua.id, CheckMode)
          case false => controllers.routes.CheckYourAnswersController.onPageLoad(ua.id)
        }
    case UnloadingCommentsPage => ua => Some(controllers.routes.CheckYourAnswersController.onPageLoad(ua.id))
    case DoYouHaveAnythingElseToReportYesNoPage =>
      ua =>
        ua.get(DoYouHaveAnythingElseToReportYesNoPage) map {
          case true  => controllers.routes.OtherThingsToReportController.onPageLoad(ua.id, CheckMode)
          case false => controllers.routes.CheckYourAnswersController.onPageLoad(ua.id)
        }
    case OtherThingsToReportPage => ua => Some(controllers.routes.CheckYourAnswersController.onPageLoad(ua.id))
    case _                       => ua => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
  }
}
