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

package controllers.transportEquipment

import config.FrontendAppConfig
import controllers.actions.*
import forms.AddAnotherFormProvider
import models.{ArrivalId, Mode}
import pages.transportEquipment.index.AddAnotherEquipmentPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transportEquipment.AddAnotherEquipmentViewModel
import viewModels.transportEquipment.AddAnotherEquipmentViewModel.AddAnotherEquipmentViewModelProvider
import views.html.transportEquipment.AddAnotherEquipmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherEquipmentController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherEquipmentView,
  viewModelProvider: AddAnotherEquipmentViewModelProvider
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherEquipmentViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, mode)
      val preparedForm = request.userAnswers.get(AddAnotherEquipmentPage) match {
        case None        => form(viewModel)
        case Some(value) => form(viewModel).fill(value)
      }
      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, viewModel))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, viewModel))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAnotherEquipmentPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield
              if (value) {
                Redirect(
                  controllers.transportEquipment.index.routes.AddContainerIdentificationNumberYesNoController.onPageLoad(arrivalId, mode, viewModel.nextIndex)
                )
              } else {
                Redirect(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
              }
        )
  }

}
