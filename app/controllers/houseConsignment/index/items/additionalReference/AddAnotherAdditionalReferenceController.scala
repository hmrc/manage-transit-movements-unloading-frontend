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

package controllers.houseConsignment.index.items.additionalReference

import config.FrontendAppConfig
import controllers.actions.*
import forms.AddAnotherFormProvider
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode}
import pages.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferencePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferenceViewModel
import viewModels.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferenceViewModel.AddAnotherAdditionalReferenceViewModelProvider
import views.html.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherAdditionalReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherAdditionalReferenceView,
  viewModelProvider: AddAnotherAdditionalReferenceViewModelProvider
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherAdditionalReferenceViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentMode: Mode, itemMode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider(request.userAnswers, arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
        val preparedForm = request.userAnswers.get(AddAnotherAdditionalReferencePage(houseConsignmentIndex, itemIndex)) match {
          case None        => form(viewModel)
          case Some(value) => form(viewModel).fill(value)
        }
        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentMode: Mode, itemMode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider(request.userAnswers, arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
        val form      = formProvider(viewModel.prefix, viewModel.allowMore, houseConsignmentIndex.display, itemIndex.display)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, viewModel))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAnotherAdditionalReferencePage(houseConsignmentIndex, itemIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield
                if (value) {
                  Redirect(
                    controllers.houseConsignment.index.items.additionalReference.routes.AdditionalReferenceTypeController
                      .onPageLoad(arrivalId, houseConsignmentMode, itemMode, NormalMode, houseConsignmentIndex, itemIndex, viewModel.nextIndex)
                  )
                } else {
                  itemMode match {
                    case CheckMode =>
                      Redirect(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
                    case NormalMode =>
                      Redirect(
                        controllers.houseConsignment.index.items.routes.AddPackagesYesNoController
                          .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
                      )
                  }
                }
          )
    }
}
