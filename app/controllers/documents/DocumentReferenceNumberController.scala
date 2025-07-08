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

import controllers.actions.*
import forms.DocumentReferenceNumberFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.DocumentNavigator
import pages.documents.DocumentReferenceNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.documents.DocumentReferenceNumberViewModel
import viewModels.documents.DocumentReferenceNumberViewModel.DocumentReferenceNumberViewModelProvider
import views.html.documents.DocumentReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DocumentReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  navigator: DocumentNavigator,
  formProvider: DocumentReferenceNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DocumentReferenceNumberView,
  viewModelProvider: DocumentReferenceNumberViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: DocumentReferenceNumberViewModel): Form[String] =
    formProvider("document.referenceNumber", viewModel.requiredError)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, documentIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(mode)
        val preparedForm = request.userAnswers.get(DocumentReferenceNumberPage(documentIndex)) match {
          case None        => form(viewModel)
          case Some(value) => form(viewModel).fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode, viewModel, documentIndex))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, documentIndex: Index): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          val viewModel = viewModelProvider.apply(mode)
          form(viewModel)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode, viewModel, documentIndex))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(DocumentReferenceNumberPage(documentIndex), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(DocumentReferenceNumberPage(documentIndex), mode, request.userAnswers))
            )
      }
}
