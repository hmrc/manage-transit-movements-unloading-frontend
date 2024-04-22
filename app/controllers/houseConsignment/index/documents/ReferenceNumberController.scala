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
import forms.{DocumentReferenceNumberFormProvider, ReferenceNumberFormProvider}
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.houseConsignment.index.documents.DocumentReferenceNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.documents.DocumentReferenceNumberViewModel.DocumentReferenceNumberViewModelProvider
import views.html.houseConsignment.index.documents.ReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  navigator: Navigator, //todo update on nav ticket
  formProvider: ReferenceNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ReferenceNumberView,
  viewModelProvider: DocumentReferenceNumberViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, documentIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(mode, houseConsignmentIndex)
        val form      = formProvider(viewModel.requiredError, mode, houseConsignmentIndex)
        val preparedForm = request.userAnswers.get(DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode, viewModel, houseConsignmentIndex, documentIndex))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, documentIndex: Index): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          val viewModel = viewModelProvider.apply(mode, houseConsignmentIndex)
          formProvider(viewModel.requiredError, mode, houseConsignmentIndex)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode, viewModel, houseConsignmentIndex, documentIndex))
                ),
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
      updatedAnswers <- Future.fromTry(request.userAnswers.set(DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex), mode, request.userAnswers))
}
