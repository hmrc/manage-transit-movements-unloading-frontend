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

import controllers.actions.{IdentifierAction, UnloadingPermissionActionProvider}
import logging.Logging
import models.P5.submission.IE044Data
import models.{ArrivalId, UserAnswers}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  unloadingPermission: UnloadingPermissionActionProvider,
  sessionRepository: SessionRepository,
  dateTimeService: DateTimeService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def unloadingRemarks(arrivalId: ArrivalId, messageId: String): Action[AnyContent] = (identify andThen unloadingPermission(arrivalId, messageId)).async {
    implicit request =>
      for {
        getUserAnswer <- sessionRepository.get(arrivalId, request.eoriNumber) map {
          _ getOrElse UserAnswers(
            id = arrivalId,
            mrn = request.unloadingPermission.TransitOperation.MRN,
            eoriNumber = request.eoriNumber,
            ie043Data = Json.toJsObject(request.unloadingPermission),
            data = Json.toJsObject(IE044Data.fromIE043Data(request.unloadingPermission)),
            lastUpdated = dateTimeService.now
          )
        }
        _ <- sessionRepository.set(getUserAnswer)
      } yield Redirect(controllers.routes.UnloadingGuidanceController.onPageLoad(arrivalId, messageId))
  }
}
