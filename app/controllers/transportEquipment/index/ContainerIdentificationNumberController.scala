/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.actions._
import forms.ContainerIdentificationNumberFormProvider
import models.requests.DataRequest
import models.{ArrivalId, Index, Mode, RichOptionalJsArray}
import navigation.TransportEquipmentNavigator
import pages.ContainerIdentificationNumberPage
import pages.sections.TransportEquipmentListSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transportEquipment.index.ContainerIdentificationNumberViewModel.ContainerIdentificationNumberViewModelProvider
import views.html.transportEquipment.index.ContainerIdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContainerIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: TransportEquipmentNavigator,
  actions: Actions,
  formProvider: ContainerIdentificationNumberFormProvider,
  viewModelProvider: ContainerIdentificationNumberViewModelProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ContainerIdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, transportEquipmentIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(mode)
        val preparedForm = request.userAnswers.get(ContainerIdentificationNumberPage(transportEquipmentIndex)) match {
          case None        => form(viewModel.requiredError, transportEquipmentIndex)
          case Some(value) => form(viewModel.requiredError, transportEquipmentIndex).fill(value)
        }
        Ok(
          view(
            preparedForm,
            arrivalId,
            request.userAnswers.mrn,
            transportEquipmentIndex,
            mode,
            viewModel
          )
        )
    }

  def onSubmit(arrivalId: ArrivalId, transportEquipmentIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider.apply(mode)
        form(viewModel.requiredError, transportEquipmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    arrivalId,
                    request.userAnswers.mrn,
                    transportEquipmentIndex,
                    mode,
                    viewModel
                  )
                )
              ),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ContainerIdentificationNumberPage(transportEquipmentIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ContainerIdentificationNumberPage(transportEquipmentIndex), mode, updatedAnswers))
          )
    }

  private def form(requiredError: String, equipmentIndex: Index)(implicit request: DataRequest[_]): Form[String] =
    formProvider(requiredError, otherContainerIdentificationNumbers(equipmentIndex))

  private def otherContainerIdentificationNumbers(equipmentIndex: Index)(implicit request: DataRequest[_]): Seq[String] = {
    val numberOfEquipments = request.userAnswers.get(TransportEquipmentListSection).length
    (0 until numberOfEquipments)
      .map(Index(_))
      .filterNot(_ == equipmentIndex)
      .map(ContainerIdentificationNumberPage)
      .flatMap(request.userAnswers.get(_))
  }

}
