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

package controllers

import connectors.ArrivalMovementConnector
import controllers.actions.IdentifierAction
import logging.Logging
import models.{ArrivalId, MovementReferenceNumber, UserAnswers}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.P5.UnloadingPermissionMessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  sessionRepository: SessionRepository,
  unloadingPermissionMessageService: UnloadingPermissionMessageService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def unloadingRemarks(arrivalId: ArrivalId): Action[AnyContent] = identify.async {
    implicit request =>
      unloadingPermissionMessageService.getUnloadingPermissionXml(arrivalId).flatMap {
        case Some(value) =>
          for {
            getUserAnswer <- sessionRepository.get(arrivalId, request.eoriNumber) map {
              _ getOrElse (UserAnswers(arrivalId, MovementReferenceNumber("35SS9OUMUBMODEESJ8").get, request.eoriNumber))
            }
            _ <- sessionRepository.set(getUserAnswer)
          } yield Redirect(controllers.p5.routes.UnloadingGuidanceController.onPageLoad(arrivalId))
        case None =>
          Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
      }
  }

}
