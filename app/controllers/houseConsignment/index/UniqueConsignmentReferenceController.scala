/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.houseConsignment.index

import controllers.actions.Actions
import forms.UniqueConsignmentReferenceFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.Navigation
import pages.houseConsignment.index.UniqueConsignmentReferencePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.UniqueConsignmentReferenceViewModel.UniqueConsignmentReferenceViewModelProvider
import views.html.houseConsignment.index.UniqueConsignmentReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UniqueConsignmentReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigation,
  actions: Actions,
  viewModelProvider: UniqueConsignmentReferenceViewModelProvider,
  ucrFormProvider: UniqueConsignmentReferenceFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UniqueConsignmentReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] =
    actions.requirePhase6(arrivalId) {
      implicit request =>
        val form      = ucrFormProvider("houseConsignment.uniqueConsignmentReference", mode)
        val viewModel = viewModelProvider.apply(mode)
        val preparedForm = request.userAnswers.get(UniqueConsignmentReferencePage(index)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, index, mode, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val form      = ucrFormProvider("houseConsignment.uniqueConsignmentReference", mode)
        val viewModel = viewModelProvider.apply(mode)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, index, mode, viewModel))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(UniqueConsignmentReferencePage(index), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(UniqueConsignmentReferencePage(index), mode, updatedAnswers))
          )
    }

}
