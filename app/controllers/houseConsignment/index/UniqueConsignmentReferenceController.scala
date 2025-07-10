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
import navigation.houseConsignment.index.HouseConsignmentNavigator
import pages.houseConsignment.index.UniqueConsignmentReferencePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.UniqueConsignmentReferenceViewModel
import viewModels.houseConsignment.index.UniqueConsignmentReferenceViewModel.UniqueConsignmentReferenceViewModelProvider
import views.html.houseConsignment.index.UniqueConsignmentReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UniqueConsignmentReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: HouseConsignmentNavigator,
  actions: Actions,
  viewModelProvider: UniqueConsignmentReferenceViewModelProvider,
  ucrFormProvider: UniqueConsignmentReferenceFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UniqueConsignmentReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "houseConsignment.uniqueConsignmentReference"

  private def form(viewModel: UniqueConsignmentReferenceViewModel): Form[String] =
    ucrFormProvider(prefix, viewModel.requiredError)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requirePhase6(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(mode, houseConsignmentIndex)
        val preparedForm = request.userAnswers.get(UniqueConsignmentReferencePage(houseConsignmentIndex)) match {
          case None        => form(viewModel)
          case Some(value) => form(viewModel).fill(value)
        }
        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, mode, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider.apply(mode, houseConsignmentIndex)
        form(viewModel)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, mode, viewModel))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(UniqueConsignmentReferencePage(houseConsignmentIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(UniqueConsignmentReferencePage(houseConsignmentIndex), mode, updatedAnswers))
          )
    }

}
