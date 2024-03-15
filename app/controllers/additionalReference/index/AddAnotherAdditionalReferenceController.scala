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

package controllers.additionalReference.index

import config.FrontendAppConfig
import controllers.actions._
import forms.AddAnotherFormProvider
import models.{ArrivalId, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.additionalReference.index.AddAnotherAdditionalReferenceViewModel
import viewModels.additionalReference.index.AddAnotherAdditionalReferenceViewModel.AddAnotherAdditionalReferenceViewModelProvider
import views.html.additionalReference.index.AddAnotherAdditionalReferenceView

import javax.inject.Inject

class AddAnotherAdditionalReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherAdditionalReferenceView,
  viewModelProvider: AddAnotherAdditionalReferenceViewModelProvider
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherAdditionalReferenceViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, mode)
      Ok(view(form(viewModel), request.userAnswers.mrn, arrivalId, viewModel))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, viewModel)),
          {
            case true =>
              Redirect(
                controllers.additionalReference.index.routes.AdditionalReferenceTypeController
                  .onPageLoad(arrivalId, mode, viewModel.nextIndex)
              )
            case false =>
              Redirect(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
          }
        )
  }
}
