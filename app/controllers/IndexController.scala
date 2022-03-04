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

import controllers.actions.{CheckArrivalStatusProvider, DataRetrievalActionProvider, IdentifierAction}

import javax.inject.Inject
import logging.Logging
import models.{ArrivalId, MovementReferenceNumber, UserAnswers}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.SealsQuery
import repositories.SessionRepository
import services.UnloadingPermissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  unloadingPermissionService: UnloadingPermissionService,
  sessionRepository: SessionRepository,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val nextPage: ArrivalId => String = arrivalId => routes.UnloadingGuidanceController.onPageLoad(arrivalId).url

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId)).async {
    implicit request =>
      request.userAnswers match {
        case Some(_) =>
          Future.successful(Redirect(nextPage(arrivalId)))
        case None =>
          unloadingPermissionService.getUnloadingPermission(arrivalId) flatMap {
            case Some(unloadingPermission) =>
              MovementReferenceNumber(unloadingPermission.movementReferenceNumber) match {
                case Some(mrn) =>
                  val updatedAnswers = request.userAnswers.getOrElse(UserAnswers(id = arrivalId, mrn = mrn, eoriNumber = request.eoriNumber))

                  val userAnswersWithPrepopulatedSeals: UserAnswers =
                    unloadingPermission.seals
                      .flatMap {
                        seals =>
                          updatedAnswers.setPrepopulateData(SealsQuery, seals.SealId).toOption
                      }
                      .getOrElse(updatedAnswers)

                  sessionRepository.set(userAnswersWithPrepopulatedSeals).flatMap {
                    _ =>
                      Future.successful(Redirect(nextPage(arrivalId)))
                  }
                case _ =>
                  logger.error(s"Failed to get validate mrn: ${unloadingPermission.movementReferenceNumber}")
                  Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
              }
            case None =>
              logger.error(s"Failed to get unloading permission for arrivalId: ${arrivalId.value}")
              Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
          }
      }
  }
}
