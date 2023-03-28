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

import com.google.inject.Inject
import connectors.ApiConnector
import controllers.actions.Actions
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.P5.UserAnswersSubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.CheckYourAnswersViewModel
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView,
  viewModelProvider: CheckYourAnswersViewModelProvider,
  apiConnector: ApiConnector
)(implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel: CheckYourAnswersViewModel = viewModelProvider.apply(request.userAnswers)

      Ok(view(request.userAnswers.mrn, arrivalId, viewModel))
  }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      for {
        userAnswers <- Future.fromTry(UserAnswersSubmissionService.userAnswersToSubmission(request.userAnswers))
        result <- {
          println(s"\n\n\n ${userAnswers.data} \n\n\n")
          apiConnector.submit(userAnswers, arrivalId)
        }
      } yield result match {
        case Left(BadRequest) => Redirect(controllers.routes.ErrorController.badRequest())
        case Left(_)          => Redirect(controllers.routes.ErrorController.technicalDifficulties())
        case Right(_)         => Redirect(controllers.routes.UnloadingRemarksSentController.onPageLoad(arrivalId))
      }
  }
}
