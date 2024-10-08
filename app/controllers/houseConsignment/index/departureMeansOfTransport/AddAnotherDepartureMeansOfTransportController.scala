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

package controllers.houseConsignment.index.departureMeansOfTransport

import config.FrontendAppConfig
import controllers.actions._
import forms.AddAnotherFormProvider
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel
import viewModels.houseConsignment.index.departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel.AddAnotherDepartureMeansOfTransportViewModelProvider
import views.html.houseConsignment.index.departureMeansOfTransport.AddAnotherDepartureMeansOfTransportView

import javax.inject.Inject

class AddAnotherDepartureMeansOfTransportController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherDepartureMeansOfTransportView,
  viewModelProvider: AddAnotherDepartureMeansOfTransportViewModelProvider
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherDepartureMeansOfTransportViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, houseConsignmentMode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, houseConsignmentIndex, houseConsignmentMode)

      Ok(view(form(viewModel), request.userAnswers.mrn, arrivalId, viewModel))
  }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, houseConsignmentMode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, houseConsignmentIndex, houseConsignmentMode)
      val form      = formProvider(viewModel.prefix, viewModel.allowMore, houseConsignmentIndex)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, viewModel)),
          {
            case true =>
              Redirect(
                controllers.houseConsignment.index.departureMeansOfTransport.routes.IdentificationController
                  .onPageLoad(arrivalId, houseConsignmentIndex, viewModel.nextIndex, houseConsignmentMode, NormalMode)
              )
            case false =>
              houseConsignmentMode match {
                case NormalMode =>
                  Redirect(
                    controllers.houseConsignment.index.routes.AddDocumentsYesNoController.onPageLoad(arrivalId, houseConsignmentMode, houseConsignmentIndex)
                  )
                case CheckMode =>
                  Redirect(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
              }
          }
        )
  }

}
