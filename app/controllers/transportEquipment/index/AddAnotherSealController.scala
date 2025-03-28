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

package controllers.transportEquipment.index

import config.FrontendAppConfig
import controllers.actions.*
import controllers.routes.*
import controllers.transportEquipment.index.seals.routes.*
import forms.AddAnotherFormProvider
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode}
import pages.transportEquipment.index.AddAnotherSealPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transportEquipment.index.AddAnotherSealViewModel
import viewModels.transportEquipment.index.AddAnotherSealViewModel.AddAnotherSealViewModelProvider
import views.html.transportEquipment.index.AddAnotherSealView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherSealController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherSealView,
  viewModelProvider: AddAnotherSealViewModelProvider
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherSealViewModel, equipmentIndex: Index): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore, equipmentIndex.display)

  def onPageLoad(arrivalId: ArrivalId, equipmentMode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, equipmentMode, equipmentIndex)
      val preparedForm = request.userAnswers.get(AddAnotherSealPage(equipmentIndex)) match {
        case None        => form(viewModel, equipmentIndex)
        case Some(value) => form(viewModel, equipmentIndex).fill(value)
      }
      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, viewModel))
  }

  def onSubmit(arrivalId: ArrivalId, equipmentMode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, equipmentMode, equipmentIndex)
      form(viewModel, equipmentIndex)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, viewModel))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAnotherSealPage(equipmentIndex), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield
              if (value) {
                Redirect(SealIdentificationNumberController.onPageLoad(arrivalId, equipmentMode, NormalMode, equipmentIndex, viewModel.nextIndex))
              } else {
                equipmentMode match {
                  case NormalMode =>
                    Redirect(routes.ApplyAnItemYesNoController.onSubmit(arrivalId, equipmentIndex, equipmentMode))
                  case CheckMode =>
                    Redirect(UnloadingFindingsController.onPageLoad(arrivalId))
                }
              }
        )
  }

}
