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
import models.{ArrivalId, MovementReferenceNumber, NormalMode, UserAnswers}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.transformers.IE043Transformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  unloadingPermission: UnloadingPermissionActionProvider,
  sessionRepository: SessionRepository,
  dateTimeService: DateTimeService,
  dataTransformer: IE043Transformer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def unloadingRemarks(arrivalId: ArrivalId, messageId: String): Action[AnyContent] = (identify andThen unloadingPermission(arrivalId)).async {
    implicit request =>
      for {
        userAnswers <- sessionRepository.get(arrivalId, request.eoriNumber).flatMap {
          case Some(value) =>
            Future.successful(value)
          case None =>
            val userAnswers = UserAnswers(
              id = arrivalId,
              mrn = MovementReferenceNumber(request.unloadingPermission.TransitOperation.MRN),
              eoriNumber = request.eoriNumber,
              ie043Data = request.unloadingPermission,
              data = Json.obj(),
              lastUpdated = dateTimeService.now
            )
            dataTransformer.transform(userAnswers)
        }
        _ <- sessionRepository.set(userAnswers)
      } yield Redirect(controllers.routes.NewAuthYesNoController.onPageLoad(arrivalId, NormalMode))
  }
}
