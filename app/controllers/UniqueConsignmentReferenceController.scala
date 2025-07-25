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

package controllers

import controllers.actions.Actions
import forms.UniqueConsignmentReferenceFormProvider
import models.{ArrivalId, Mode}
import navigation.ConsignmentNavigator
import pages.UniqueConsignmentReferencePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.UniqueConsignmentReferenceViewModel
import viewModels.UniqueConsignmentReferenceViewModel.UniqueConsignmentReferenceViewModelProvider
import views.html.UniqueConsignmentReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UniqueConsignmentReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ConsignmentNavigator,
  actions: Actions,
  viewModelProvider: UniqueConsignmentReferenceViewModelProvider,
  ucrFormProvider: UniqueConsignmentReferenceFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UniqueConsignmentReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "uniqueConsignmentReference"

  private def form(viewModel: UniqueConsignmentReferenceViewModel): Form[String] =
    ucrFormProvider(prefix, viewModel.requiredError)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    actions.requirePhase6(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(mode)
        val preparedForm = request.userAnswers.get(UniqueConsignmentReferencePage) match {
          case None        => form(viewModel)
          case Some(value) => form(viewModel).fill(value)
        }
        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider.apply(mode)
        form(viewModel)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, viewModel))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(UniqueConsignmentReferencePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(UniqueConsignmentReferencePage, mode, updatedAnswers))
          )
    }

}
