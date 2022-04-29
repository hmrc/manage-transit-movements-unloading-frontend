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

package controllers

import controllers.actions.Actions
import extractors.UnloadingPermissionExtractor
import logging.Logging
import models.{ArrivalId, MovementReferenceNumber, UserAnswers}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.UnloadingPermissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  unloadingPermissionService: UnloadingPermissionService,
  sessionRepository: SessionRepository,
  unloadingPermissionExtractor: UnloadingPermissionExtractor
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val redirect: ArrivalId => Result = arrivalId => Redirect(routes.UnloadingGuidanceController.onPageLoad(arrivalId))

  def unloadingRemarks(arrivalId: ArrivalId): Action[AnyContent] = actions.getData(arrivalId).async {
    implicit request =>
      request.userAnswers match {
        case Some(_) =>
          Future.successful(redirect(arrivalId))
        case None =>
          unloadingPermissionService.getUnloadingPermission(arrivalId) flatMap {
            case Some(unloadingPermission) =>
              MovementReferenceNumber(unloadingPermission.movementReferenceNumber) match {
                case Some(mrn) =>
                  val userAnswers = UserAnswers(id = arrivalId, mrn = mrn, eoriNumber = request.eoriNumber)
                  unpackUnloadingPermission(arrivalId, userAnswers)
                case None =>
                  logger.error(s"Failed to get validate mrn: ${unloadingPermission.movementReferenceNumber}")
                  Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
              }
            case None =>
              logger.error(s"Failed to get unloading permission for arrivalId: ${arrivalId.value}")
              Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
          }
      }
  }

  def newUnloadingRemarks(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      unpackUnloadingPermission(arrivalId, request.userAnswers)
  }

  private def unpackUnloadingPermission(
    arrivalId: ArrivalId,
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[Result] =
    unloadingPermissionService.getUnloadingPermission(arrivalId) flatMap {
      case Some(unloadingPermission) =>
        unloadingPermissionExtractor(userAnswers, unloadingPermission).flatMap {
          case Success(updatedAnswers) =>
            sessionRepository.set(updatedAnswers).map {
              _ => redirect(arrivalId)
            }
          case Failure(exception) =>
            logger.error(s"Failed to extract unloading permission data to user answers: ${exception.getMessage}")
            Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
        }
      case None =>
        logger.error(s"Failed to get unloading permission for arrivalId: ${arrivalId.value}")
        Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
    }
}
