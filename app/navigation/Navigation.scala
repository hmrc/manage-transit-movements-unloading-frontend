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
import models.{CheckMode, Mode, NormalMode, RichCC043CType, StateOfSeals, UserAnswers}
import pages.*
import play.api.mvc.Call

@Singleton
class Navigation extends Navigator {

  // scalastyle:off cyclomatic.complexity
  override def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case NewAuthYesNoPage                                    => ua => newAuthNavigation(ua, NormalMode)
    case UnloadingTypePage                                   => ua => Some(routes.DateGoodsUnloadedController.onPageLoad(ua.id, NormalMode))
    case DateGoodsUnloadedPage                               => ua => dateGoodsUnloadedNavigation(ua)
    case CanSealsBeReadPage                                  => ua => Some(routes.AreAnySealsBrokenController.onPageLoad(ua.id, NormalMode))
    case AreAnySealsBrokenPage                               => ua => stateOfSealsNormalNavigation(ua)
    case AddTransitUnloadingPermissionDiscrepanciesYesNoPage => ua => anyDiscrepanciesNavigation(ua, NormalMode)
    case AddCommentsYesNoPage                                => ua => addCommentsNavigation(ua, NormalMode)
    case UnloadingCommentsPage                               => ua => Some(routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(ua.id, NormalMode))
    case DoYouHaveAnythingElseToReportYesNoPage              => ua => anythingElseToReportNavigation(ua, NormalMode)
    case OtherThingsToReportPage                             => ua => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
    case GoodsTooLargeForContainerYesNoPage                  => ua => goodsTooLargeForContainerNavigation(ua, NormalMode)
    case LargeUnsealedGoodsRecordDiscrepanciesYesNoPage      => ua => largeUnsealedGoodsDiscrepanciesYesNoNavigation(ua)
    case SealsReplacedByCustomsAuthorityYesNoPage            => ua => sealsReplacedNavigation(ua, NormalMode)
    case RevisedUnloadingProcedureConditionsYesNoPage        => ua => revisedUnloadingProcedureConditionsYesNoNavigation(ua)
  }
  // scalastyle:on cyclomatic.complexity

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case NewAuthYesNoPage                                    => ua => newAuthNavigation(ua, CheckMode)
    case RevisedUnloadingProcedureConditionsYesNoPage        => ua => revisedUnloadingProcedureConditionsYesNoNavigation(ua)
    case CanSealsBeReadPage | AreAnySealsBrokenPage          => ua => stateOfSealsCheckNavigation(ua)
    case AddTransitUnloadingPermissionDiscrepanciesYesNoPage => ua => anyDiscrepanciesNavigation(ua, CheckMode)
    case AddCommentsYesNoPage                                => ua => addCommentsNavigation(ua, CheckMode)
    case DoYouHaveAnythingElseToReportYesNoPage              => ua => anythingElseToReportNavigation(ua, CheckMode)
    case GrossWeightPage                                     => ua => Some(routes.UnloadingFindingsController.onPageLoad(ua.id))
    case GoodsTooLargeForContainerYesNoPage                  => ua => goodsTooLargeForContainerNavigation(ua, CheckMode)
    case LargeUnsealedGoodsRecordDiscrepanciesYesNoPage      => ua => largeUnsealedGoodsDiscrepanciesYesNoNavigation(ua)
    case SealsReplacedByCustomsAuthorityYesNoPage            => ua => sealsReplacedNavigation(ua, CheckMode)
    case _                                                   => ua => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
  }

  private def newAuthNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    mode match {
      case NormalMode =>
        ua.get(NewAuthYesNoPage).map {
          case true =>
            routes.RevisedUnloadingProcedureConditionsYesNoController.onPageLoad(ua.id, NormalMode)
          case false =>
            routes.UnloadingGuidanceController.onPageLoad(ua.id)
        }
      case CheckMode =>
        ua.get(NewAuthYesNoPage).map {
          case true =>
            routes.RevisedUnloadingProcedureConditionsYesNoController.onPageLoad(ua.id, NormalMode)
          case false =>
            ua.get(UnloadingTypePage)
              .fold {
                routes.UnloadingGuidanceController.onPageLoad(ua.id)
              } {
                _ => routes.CheckYourAnswersController.onPageLoad(ua.id)
              }
        }
    }

  private def goodsTooLargeForContainerNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    mode match {
      case NormalMode =>
        Some(routes.UnloadingGuidanceController.onPageLoad(ua.id))
      case CheckMode =>
        ua.get(GoodsTooLargeForContainerYesNoPage).map {
          case true =>
            ua.get(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage)
              .fold {
                routes.UnloadingGuidanceController.onPageLoad(ua.id)
              } {
                _ => routes.CheckYourAnswersController.onPageLoad(ua.id)
              }
          case false =>
            ua.get(SealsReplacedByCustomsAuthorityYesNoPage)
              .fold {
                routes.UnloadingGuidanceController.onPageLoad(ua.id)
              } {
                _ => routes.CheckYourAnswersController.onPageLoad(ua.id)
              }
        }
    }

  private def sealsReplacedNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    mode match {
      case NormalMode =>
        Some(routes.OtherThingsToReportController.onPageLoad(ua.id, NormalMode))
      case CheckMode =>
        ua.get(OtherThingsToReportPage)
          .fold {
            Some(routes.OtherThingsToReportController.onPageLoad(ua.id, NormalMode))
          } {
            _ => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
          }
    }

  private def largeUnsealedGoodsDiscrepanciesYesNoNavigation(ua: UserAnswers): Option[Call] =
    ua.get(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage).map {
      case true =>
        routes.CannotUseRevisedUnloadingProcedureController.onPageLoad(ua.id)
      case false =>
        routes.CheckYourAnswersController.onPageLoad(ua.id)
    }

  private def stateOfSealsNormalNavigation(ua: UserAnswers): Option[Call] =
    ua.get(NewAuthYesNoPage) match
      case Some(false) => Some(routes.UnloadingFindingsController.onPageLoad(ua.id))
      case _ =>
        StateOfSeals(ua).value match {
          case Some(true) =>
            Some(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(ua.id, NormalMode))
          case _ =>
            Some(routes.UnloadingFindingsController.onPageLoad(ua.id))
        }

  private def stateOfSealsCheckNavigation(ua: UserAnswers): Option[Call] =
    StateOfSeals(ua).value match {
      case Some(true) =>
        ua.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) match {
          case Some(_) =>
            Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
          case None =>
            Some(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(ua.id, CheckMode))
        }
      case _ =>
        Some(routes.UnloadingFindingsController.onPageLoad(ua.id))
    }

  private def addCommentsNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    ua.get(AddCommentsYesNoPage) map {
      case true =>
        routes.UnloadingCommentsController.onPageLoad(ua.id, mode)
      case false =>
        mode match {
          case NormalMode =>
            routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(ua.id, mode)
          case CheckMode =>
            routes.CheckYourAnswersController.onPageLoad(ua.id)
        }
    }

  private def anythingElseToReportNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    ua.get(DoYouHaveAnythingElseToReportYesNoPage) map {
      case true  => routes.OtherThingsToReportController.onPageLoad(ua.id, mode)
      case false => routes.CheckYourAnswersController.onPageLoad(ua.id)
    }

  private def anyDiscrepanciesNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    ua.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) map {
      case true =>
        routes.UnloadingFindingsController.onPageLoad(ua.id)
      case false =>
        mode match {
          case NormalMode =>
            routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(ua.id, mode)
          case CheckMode =>
            routes.CheckYourAnswersController.onPageLoad(ua.id)
        }
    }

  private def dateGoodsUnloadedNavigation(ua: UserAnswers): Option[Call] =
    if (ua.ie043Data.sealsExist) {
      Some(routes.CanSealsBeReadController.onPageLoad(ua.id, NormalMode))
    } else {
      Some(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(ua.id, NormalMode))
    }

  private def revisedUnloadingProcedureConditionsYesNoNavigation(ua: UserAnswers): Option[Call] =
    ua.get(RevisedUnloadingProcedureConditionsYesNoPage).map {
      case true =>
        routes.GoodsTooLargeForContainerYesNoController.onPageLoad(ua.id, NormalMode)
      case false =>
        routes.RevisedUnloadingProcedureUnmetConditionsController.onPageLoad(ua.id)
    }
}
