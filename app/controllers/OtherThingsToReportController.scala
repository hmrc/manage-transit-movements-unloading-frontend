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

import controllers.actions.*
import forms.Constants.otherThingsToReportLength
import forms.OtherThingsToReportFormProvider
import models.{ArrivalId, Mode}
import navigation.Navigation
import pages.OtherThingsToReportPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.OtherThingsToReportViewModel
import viewModels.OtherThingsToReportViewModel.OtherThingsToReportViewModelProvider
import views.html.OtherThingsToReportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherThingsToReportController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigation,
  actions: Actions,
  formProvider: OtherThingsToReportFormProvider,
  val controllerComponents: MessagesControllerComponents,
  viewModelProvider: OtherThingsToReportViewModelProvider,
  view: OtherThingsToReportView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: OtherThingsToReportViewModel): Form[String] =
    formProvider(viewModel.requiredError, viewModel.maxLengthError, viewModel.invalidError)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider(request.userAnswers, arrivalId, mode)
        val preparedForm = request.userAnswers.get(OtherThingsToReportPage) match {
          case None        => form(viewModel)
          case Some(value) => form(viewModel).fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, otherThingsToReportLength, mode, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider(request.userAnswers, arrivalId, mode)
        form(viewModel)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, otherThingsToReportLength, mode, viewModel))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(OtherThingsToReportPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(OtherThingsToReportPage, mode, updatedAnswers))
          )
    }
}
