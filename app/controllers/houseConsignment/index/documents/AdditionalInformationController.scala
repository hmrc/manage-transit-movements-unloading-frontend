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

import controllers.actions._
import forms.DocumentsAdditionalInformationFormProvider
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.Navigation
import navigation.houseConsignment.index.HouseConsignmentNavigator
import pages.houseConsignment.index.documents.AdditionalInformationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.documents.AdditionalInformationViewModel.AdditionalInformationViewModelProvider
import views.html.houseConsignment.index.documents.AdditionalInformationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  formProvider: DocumentsAdditionalInformationFormProvider,
  navigator: HouseConsignmentNavigator,
  view: AdditionalInformationView,
  viewModelProvider: AdditionalInformationViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, documentIndex: Index): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider.apply(mode, houseConsignmentIndex)
      val preparedForm = request.userAnswers.get(AdditionalInformationPage(houseConsignmentIndex, documentIndex)) match {
        case None        => formProvider(viewModel.requiredError)
        case Some(value) => formProvider(viewModel.requiredError).fill(value)
      }
      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode, viewModel, houseConsignmentIndex, documentIndex))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, documentIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider.apply(mode, houseConsignmentIndex)
        formProvider(viewModel.requiredError)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode, viewModel, houseConsignmentIndex, documentIndex))),
            value => redirect(mode, value, houseConsignmentIndex, documentIndex)
          )
    }

  private def redirect(
    mode: Mode,
    value: String,
    houseConsignmentIndex: Index,
    documentIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AdditionalInformationPage(houseConsignmentIndex, documentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AdditionalInformationPage(houseConsignmentIndex, documentIndex), mode, request.userAnswers))
}
