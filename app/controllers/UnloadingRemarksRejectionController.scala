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

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import logging.Logging
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.UnloadingRemarksRejectionViewModel

import scala.concurrent.ExecutionContext

class UnloadingRemarksRejectionController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  val renderer: Renderer,
  val appConfig: FrontendAppConfig,
  service: UnloadingRemarksRejectionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging
    with TechnicalDifficultiesPage {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = identify.async {
    implicit request =>
      service.unloadingRemarksRejectionMessage(arrivalId) flatMap {
        case Some(rejectionMessage) =>
          UnloadingRemarksRejectionViewModel(rejectionMessage.errors, arrivalId, appConfig.nctsEnquiriesUrl) match {
            case Some(viewModel) => renderer.render(viewModel.page, viewModel.json).map(Ok(_))
            case _ =>
              logger.debug(s"Couldn't build a UnloadingRemarksRejectionViewModel for arrival: $arrivalId")
              renderTechnicalDifficultiesPage
          }
        case _ =>
          logger.error(s"Failed to pull back a rejection message for arrival: $arrivalId")
          renderTechnicalDifficultiesPage
      }
  }
}
