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

package controllers.transportEquipment.index.seals

import controllers.actions._
import forms.SealIdentificationNumberFormProvider
import models.requests.{DataRequest, MandatoryDataRequest}
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode, RichOptionalJsArray}
import navigation.Navigator
import pages.sections.SealsSection
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transportEquipment.index.seals.SealIdentificationNumberViewModel.SealIdentificationNumberViewModelProvider
import views.html.transportEquipment.index.seals.SealIdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SealIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  navigator: Navigator,
  formProvider: SealIdentificationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SealIdentificationNumberView,
  viewModelProvider: SealIdentificationNumberViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def prefix(mode: Mode): String = mode match {
    case NormalMode => "transportEquipment.index.seal.identificationNumber.NormalMode"
    case CheckMode  => "transportEquipment.index.seal.identificationNumber.CheckMode"
  }

  private def form(mode: Mode, equipmentIndex: Index, sealIndex: Index)(implicit request: DataRequest[_]) =
    formProvider(prefix(mode), otherSealIdentificationNumbers(equipmentIndex, sealIndex))

  private def otherSealIdentificationNumbers(equipmentIndex: Index, sealIndex: Index)(implicit request: DataRequest[_]): Seq[String] = {
    val numberOfSeals = request.userAnswers.get(SealsSection(equipmentIndex)).length
    (0 until numberOfSeals)
      .filterNot(_ == sealIndex.position)
      .map(
        sealInd => SealIdentificationNumberPage(equipmentIndex, Index(sealInd))
      )
      .flatMap(request.userAnswers.get(_))
  }

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index, sealIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(mode)
        val preparedForm = request.userAnswers.get(SealIdentificationNumberPage(equipmentIndex, sealIndex)) match {
          case None        => form(mode, equipmentIndex, sealIndex)
          case Some(value) => form(mode, equipmentIndex, sealIndex).fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode, viewModel, equipmentIndex, sealIndex))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index, sealIndex: Index): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          val viewModel = viewModelProvider.apply(mode)
          form(mode, equipmentIndex, sealIndex)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode, viewModel, equipmentIndex, sealIndex))),
              value => redirect(mode, value, equipmentIndex, sealIndex)
            )

      }

  private def redirect(
    mode: Mode,
    value: String,
    equipmentIndex: Index,
    sealIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(SealIdentificationNumberPage(equipmentIndex, sealIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(SealIdentificationNumberPage(equipmentIndex, sealIndex), mode, request.userAnswers))
}
