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

package controllers.houseConsignment.index.documents

import config.FrontendAppConfig
import controllers.actions.*
import forms.AddAnotherFormProvider
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode}
import pages.houseConsignment.index.documents.AddAnotherDocumentPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.documents.AddAnotherHouseConsignmentDocumentViewModel
import viewModels.houseConsignment.index.documents.AddAnotherHouseConsignmentDocumentViewModel.*
import views.html.houseConsignment.index.documents.AddAnotherDocumentView

import javax.inject.Inject

class AddAnotherDocumentController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherDocumentView,
  viewModelProvider: AddAnotherHouseConsignmentDocumentViewModelProvider
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherHouseConsignmentDocumentViewModel, houseConsignmentIndex: Index): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore, houseConsignmentIndex.display)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, houseConsignmentIndex, mode)
      val preparedForm = request.userAnswers.get(AddAnotherDocumentPage(houseConsignmentIndex)) match {
        case None        => form(viewModel, houseConsignmentIndex)
        case Some(value) => form(viewModel, houseConsignmentIndex).fill(value)
      }
      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, viewModel))
  }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, houseConsignmentIndex, mode)
      form(viewModel, houseConsignmentIndex)
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, viewModel)),
          {
            case true =>
              Redirect(
                controllers.houseConsignment.index.documents.routes.TypeController
                  .onPageLoad(arrivalId, mode, NormalMode, houseConsignmentIndex, viewModel.nextIndex)
              )
            case false =>
              mode match {
                case NormalMode =>
                  Redirect(controllers.houseConsignment.index.routes.AddAdditionalReferenceYesNoController.onPageLoad(arrivalId, mode, houseConsignmentIndex))
                case CheckMode =>
                  Redirect(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
              }
          }
        )
  }
}
