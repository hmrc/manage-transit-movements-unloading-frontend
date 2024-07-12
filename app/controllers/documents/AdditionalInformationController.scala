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

package controllers.documents

import controllers.actions._
import forms.AdditionalInformationFormProvider
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.DocumentNavigator
import pages.documents.AdditionalInformationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.documents.AdditionalInformationViewModel.AdditionalInformationViewModelProvider
import views.html.documents.AdditionalInformationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  formProvider: AdditionalInformationFormProvider,
  navigator: DocumentNavigator,
  view: AdditionalInformationView,
  viewModelProvider: AdditionalInformationViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider.apply(mode)
      val preparedForm = request.userAnswers.get(AdditionalInformationPage(documentIndex)) match {
        case None        => formProvider(viewModel.requiredError)
        case Some(value) => formProvider(viewModel.requiredError).fill(value)
      }
      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode, viewModel, documentIndex))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      val viewModel = viewModelProvider.apply(mode)
      formProvider(viewModel.requiredError)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode, viewModel, documentIndex))),
          value => redirect(mode, value, documentIndex)
        )
  }

  private def redirect(
    mode: Mode,
    value: String,
    documentIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AdditionalInformationPage(documentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AdditionalInformationPage(documentIndex), mode, request.userAnswers))
}
