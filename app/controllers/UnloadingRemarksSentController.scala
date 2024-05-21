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

import cats.data.OptionT
import logging.Logging
import models.{ArrivalId, UnloadingRemarksSentViewModel}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.P5.UnloadingPermissionMessageService
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.UnloadingRemarksSentView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UnloadingRemarksSentController @Inject() (
  override val messagesApi: MessagesApi,
  referenceDataService: ReferenceDataService,
  cc: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  messageService: UnloadingPermissionMessageService,
  view: UnloadingRemarksSentView
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport
    with Logging {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = Action.async {
    implicit request =>
      (
        for {
          ie044 <- OptionT(messageService.getIE044(arrivalId))
          customsOfficeId = ie044.CustomsOfficeOfDestinationActual.referenceNumber
          customsOffice <- OptionT.liftF(referenceDataService.getCustomsOfficeByCode(customsOfficeId))
          _             <- OptionT.liftF(sessionRepository.remove(arrivalId))
        } yield Ok(view(ie044.TransitOperation.MRN, UnloadingRemarksSentViewModel(customsOffice, customsOfficeId)))
      ).getOrElse {
        logger.warn(s"No IE044 message found for arrival ID $arrivalId")
        Redirect(controllers.routes.ErrorController.technicalDifficulties())
      }
  }
}
