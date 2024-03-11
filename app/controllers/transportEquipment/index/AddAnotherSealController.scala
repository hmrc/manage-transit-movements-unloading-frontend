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
import controllers.actions._
import forms.AddAnotherFormProvider
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.TransportEquipmentNavigator
import pages.transportEquipment.index.AddAnotherSealPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transportEquipment.index.AddAnotherSealViewModel
import viewModels.transportEquipment.index.AddAnotherSealViewModel.AddAnotherSealViewModelProvider
import views.html.transportEquipment.index.AddAnotherSealView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherSealController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  navigator: TransportEquipmentNavigator,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherSealView,
  viewModelProvider: AddAnotherSealViewModelProvider
)(implicit config: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherSealViewModel, equipmentIndex: Index): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore, equipmentIndex.display)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, mode, equipmentIndex)
      Ok(view(form(viewModel, equipmentIndex), request.userAnswers.mrn, arrivalId, viewModel))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, mode, equipmentIndex)
      form(viewModel, equipmentIndex)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, viewModel))),
          value => redirect(mode, value, equipmentIndex, viewModel.nextIndex)
        )
  }

  def redirect(mode: Mode, value: Boolean, equipmentIndex: Index, sealIndex: Index)(implicit
    request: MandatoryDataRequest[_]
  ): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAnotherSealPage(equipmentIndex, sealIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AddAnotherSealPage(equipmentIndex, sealIndex), mode, updatedAnswers))

}
