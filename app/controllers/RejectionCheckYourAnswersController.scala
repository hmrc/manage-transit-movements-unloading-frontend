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
import controllers.actions.{CheckArrivalStatusProvider, DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import handlers.ErrorHandler
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.UnloadingRemarksService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.RejectionCheckYourAnswersViewModel
import viewModels.sections.Section

import scala.concurrent.{ExecutionContext, Future}

class RejectionCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  unloadingRemarksService: UnloadingRemarksService,
  val controllerComponents: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  val renderer: Renderer,
  val appConfig: FrontendAppConfig,
  auditEventSubmissionService: AuditEventSubmissionService,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with TechnicalDifficultiesPage {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        val viewModel             = RejectionCheckYourAnswersViewModel(request.userAnswers)
        val answers: Seq[Section] = viewModel.sections
        renderer
          .render(
            "rejection-check-your-answers.njk",
            Json
              .obj("mrn" -> request.userAnswers.mrn, "sections" -> Json.toJson(answers), "arrivalId" -> arrivalId)
          )
          .map(Ok(_))
    }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        unloadingRemarksService.resubmit(arrivalId, request.userAnswers) flatMap {
          case Some(status) =>
            status match {
              case ACCEPTED =>
                auditEventSubmissionService.auditUnloadingRemarks(request.userAnswers, "resubmitUnloadingRemarks")
                Future.successful(Redirect(routes.ConfirmationController.onPageLoad(arrivalId)))
              case UNAUTHORIZED => errorHandler.onClientError(request, UNAUTHORIZED)
              case _            => renderTechnicalDifficultiesPage
            }
          case None => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
        }

    }

}
