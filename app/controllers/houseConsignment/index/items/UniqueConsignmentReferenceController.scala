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

package controllers.houseConsignment.index.items

import controllers.actions.Actions
import forms.UniqueConsignmentReferenceFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator.HouseConsignmentItemNavigatorProvider
import pages.houseConsignment.index.items.UniqueConsignmentReferencePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.UniqueConsignmentReferenceViewModel
import viewModels.houseConsignment.index.items.UniqueConsignmentReferenceViewModel.UniqueConsignmentReferenceViewModelProvider
import views.html.houseConsignment.index.items.UniqueConsignmentReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UniqueConsignmentReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: HouseConsignmentItemNavigatorProvider,
  actions: Actions,
  viewModelProvider: UniqueConsignmentReferenceViewModelProvider,
  ucrFormProvider: UniqueConsignmentReferenceFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UniqueConsignmentReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "houseConsignment.item.uniqueConsignmentReference"

  private def form(viewModel: UniqueConsignmentReferenceViewModel, houseConsignmentIndex: Index, itemIndex: Index): Form[String] =
    ucrFormProvider(prefix, viewModel.requiredError, houseConsignmentIndex, itemIndex)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentMode: Mode, itemMode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Action[AnyContent] =
    actions.requirePhase6(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
        val preparedForm = request.userAnswers.get(UniqueConsignmentReferencePage(houseConsignmentIndex, itemIndex)) match {
          case None        => form(viewModel, houseConsignmentIndex, itemIndex)
          case Some(value) => form(viewModel, houseConsignmentIndex, itemIndex).fill(value)
        }
        Ok(view(preparedForm, request.userAnswers.mrn, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentMode: Mode, itemMode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider.apply(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
        form(viewModel, houseConsignmentIndex, itemIndex)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, viewModel))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(UniqueConsignmentReferencePage(houseConsignmentIndex, itemIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield {
                val navigator = navigatorProvider.apply(houseConsignmentMode)
                Redirect(navigator.nextPage(UniqueConsignmentReferencePage(houseConsignmentIndex, itemIndex), itemMode, request.userAnswers))
              }
          )
    }
}
