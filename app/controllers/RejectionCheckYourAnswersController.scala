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

import audit.services.AuditEventSubmissionService
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.Actions
import handlers.ErrorHandler
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{UnloadingPermissionService, UnloadingRemarksService}
import uk.gov.hmrc.http.HttpErrorFunctions
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.RejectionCheckYourAnswersViewModel
import views.html.RejectionCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class RejectionCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  unloadingPermissionService: UnloadingPermissionService,
  unloadingRemarksService: UnloadingRemarksService,
  val controllerComponents: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  val appConfig: FrontendAppConfig,
  auditEventSubmissionService: AuditEventSubmissionService,
  view: RejectionCheckYourAnswersView,
  viewModel: RejectionCheckYourAnswersViewModel
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with HttpErrorFunctions {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val sections = viewModel(request.userAnswers)
      Ok(view(request.userAnswers.mrn, arrivalId, sections))
  }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      unloadingRemarksService.resubmit(arrivalId, request.userAnswers) flatMap {
        case Some(status) if is2xx(status) =>
          auditEventSubmissionService.auditUnloadingRemarks(None, "resubmitUnloadingRemarks")
          Future.successful(Redirect(routes.ConfirmationController.onPageLoad(arrivalId)))
        case Some(status) if is4xx(status) => errorHandler.onClientError(request, status)
        case _                             => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
      }
  }

}
