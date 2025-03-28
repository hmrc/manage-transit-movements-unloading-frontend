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
import models.Procedure.*
import models.{CheckMode, Mode, NormalMode, Procedure, RichCC043CType, StateOfSeals, UserAnswers}
import pages.*
import play.api.libs.json.Reads
import play.api.mvc.Call

@Singleton
class Navigation extends Navigator {

  // scalastyle:off cyclomatic.complexity
  override def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case NewAuthYesNoPage                                    => ua => newAuthNavigation(ua, NormalMode)
    case UnloadingTypePage                                   => ua => Some(routes.DateGoodsUnloadedController.onPageLoad(ua.id, NormalMode))
    case DateGoodsUnloadedPage                               => ua => dateGoodsUnloadedNavigation(ua)
    case CanSealsBeReadPage                                  => ua => Some(routes.AreAnySealsBrokenController.onPageLoad(ua.id, NormalMode))
    case AreAnySealsBrokenPage                               => ua => stateOfSealsNavigation(ua, NormalMode)
    case AddTransitUnloadingPermissionDiscrepanciesYesNoPage => ua => anyDiscrepanciesNavigation(ua, NormalMode)
    case UnloadingCommentsPage                               => ua => Some(routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(ua.id, NormalMode))
    case DoYouHaveAnythingElseToReportYesNoPage              => ua => anythingElseToReportNavigation(ua, NormalMode)
    case OtherThingsToReportPage                             => ua => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
    case GoodsTooLargeForContainerYesNoPage                  => ua => goodsTooLargeForContainerNavigation(ua, NormalMode)
    case SealsReplacedByCustomsAuthorityYesNoPage            => ua => sealsReplacedNavigation(ua, NormalMode)
    case RevisedUnloadingProcedureConditionsYesNoPage        => ua => revisedUnloadingProcedureConditionsYesNoNavigation(ua, NormalMode)
  }
  // scalastyle:on cyclomatic.complexity

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case NewAuthYesNoPage                                    => ua => newAuthNavigation(ua, CheckMode)
    case RevisedUnloadingProcedureConditionsYesNoPage        => ua => revisedUnloadingProcedureConditionsYesNoNavigation(ua, CheckMode)
    case CanSealsBeReadPage | AreAnySealsBrokenPage          => ua => stateOfSealsNavigation(ua, CheckMode)
    case AddTransitUnloadingPermissionDiscrepanciesYesNoPage => ua => anyDiscrepanciesNavigation(ua, CheckMode)
    case DoYouHaveAnythingElseToReportYesNoPage              => ua => anythingElseToReportNavigation(ua, CheckMode)
    case GrossWeightPage                                     => ua => Some(routes.UnloadingFindingsController.onPageLoad(ua.id))
    case GoodsTooLargeForContainerYesNoPage                  => ua => goodsTooLargeForContainerNavigation(ua, CheckMode)
    case SealsReplacedByCustomsAuthorityYesNoPage            => ua => sealsReplacedNavigation(ua, CheckMode)
    case _                                                   => ua => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
  }

  private def newAuthNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    ua.get(NewAuthYesNoPage).map {
      case true =>
        navigateToNext(
          ua,
          mode,
          RevisedUnloadingProcedureConditionsYesNoPage,
          routes.RevisedUnloadingProcedureConditionsYesNoController.onPageLoad(ua.id, mode)
        )
      case false =>
        navigateToNext(ua, mode, UnloadingTypePage, routes.UnloadingGuidanceController.onPageLoad(ua.id))
    }

  private def goodsTooLargeForContainerNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    mode match {
      case NormalMode =>
        Some(routes.UnloadingGuidanceController.onPageLoad(ua.id))
      case CheckMode =>
        ua.get(GoodsTooLargeForContainerYesNoPage).map {
          case true =>
            navigateToNext(ua, AddTransitUnloadingPermissionDiscrepanciesYesNoPage, routes.UnloadingGuidanceController.onPageLoad(ua.id))
          case false =>
            navigateToNext(ua, SealsReplacedByCustomsAuthorityYesNoPage, routes.UnloadingGuidanceController.onPageLoad(ua.id))
        }
    }

  private def sealsReplacedNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    Some(navigateToNext(ua, mode, OtherThingsToReportPage, routes.OtherThingsToReportController.onPageLoad(ua.id, NormalMode)))

  private def stateOfSealsNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    Some {
      Procedure(ua) match {
        case CannotUseRevisedDueToDiscrepancies =>
          mode match {
            case NormalMode => routes.UnloadingFindingsController.onPageLoad(ua.id)
            case CheckMode  => routes.CheckYourAnswersController.onPageLoad(ua.id)
          }
        case _ =>
          StateOfSeals(ua).value match {
            case Some(true) =>
              navigateToNext(
                ua,
                AddTransitUnloadingPermissionDiscrepanciesYesNoPage,
                routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(ua.id, mode)
              )
            case _ =>
              navigateToNext(ua, mode, UnloadingCommentsPage, routes.UnloadingFindingsController.onPageLoad(ua.id))
          }
      }
    }

  private def anythingElseToReportNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    ua.get(DoYouHaveAnythingElseToReportYesNoPage) map {
      case true =>
        navigateToNext(ua, mode, OtherThingsToReportPage, routes.OtherThingsToReportController.onPageLoad(ua.id, mode))
      case false =>
        routes.CheckYourAnswersController.onPageLoad(ua.id)
    }

  private def anyDiscrepanciesNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    Procedure(ua) match {
      case CannotUseRevisedDueToDiscrepancies =>
        Some(routes.CannotUseRevisedUnloadingProcedureController.onPageLoad(ua.id))
      case RevisedAndGoodsTooLarge =>
        Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _ =>
        ua.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) map {
          case true =>
            navigateToNext(
              ua,
              mode,
              UnloadingCommentsPage,
              routes.UnloadingFindingsController.onPageLoad(ua.id)
            )
          case false =>
            navigateToNext(
              ua,
              mode,
              DoYouHaveAnythingElseToReportYesNoPage,
              routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(ua.id, mode)
            )
        }
    }

  private def dateGoodsUnloadedNavigation(ua: UserAnswers): Option[Call] =
    if (ua.ie043Data.sealsExist) {
      Some(routes.CanSealsBeReadController.onPageLoad(ua.id, NormalMode))
    } else {
      Some(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(ua.id, NormalMode))
    }

  private def revisedUnloadingProcedureConditionsYesNoNavigation(ua: UserAnswers, mode: Mode): Option[Call] =
    ua.get(RevisedUnloadingProcedureConditionsYesNoPage).map {
      case true =>
        navigateToNext(ua, mode, GoodsTooLargeForContainerYesNoPage, routes.GoodsTooLargeForContainerYesNoController.onPageLoad(ua.id, mode))
      case false =>
        navigateToNext(ua, mode, UnloadingTypePage, routes.RevisedUnloadingProcedureUnmetConditionsController.onPageLoad(ua.id))
    }

  private def navigateToNext[T](
    ua: UserAnswers,
    mode: Mode,
    nextPageToAnswer: QuestionPage[T],
    nextPageSequentially: Call
  )(implicit reads: Reads[T]): Call =
    mode match
      case NormalMode =>
        nextPageSequentially
      case CheckMode =>
        navigateToNext(ua, nextPageToAnswer, nextPageSequentially)

  private def navigateToNext[T](
    ua: UserAnswers,
    nextPageToAnswer: QuestionPage[T],
    nextPageSequentially: Call
  )(implicit reads: Reads[T]): Call =
    ua.get(nextPageToAnswer)
      .fold {
        nextPageSequentially
      } {
        _ => routes.CheckYourAnswersController.onPageLoad(ua.id)
      }
}
