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

package controllers.houseConsignment.index.items.packages

import config.FrontendAppConfig
import controllers.actions.*
import forms.AddAnotherFormProvider
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode}
import pages.houseConsignment.index.items.packages.AddAnotherPackagePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.packages.AddAnotherPackageViewModel
import viewModels.houseConsignment.index.items.packages.AddAnotherPackageViewModel.AddAnotherPackageViewModelProvider
import views.html.houseConsignment.index.items.packages.AddAnotherPackageView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherPackageController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherPackageView,
  viewModelProvider: AddAnotherPackageViewModelProvider
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherPackageViewModel, houseConsignmentIndex: Index, itemIndex: Index): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore, viewModel.count, itemIndex.display, houseConsignmentIndex.display)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, houseConsignmentMode: Mode, itemMode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider(request.userAnswers, arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
        val preparedForm = request.userAnswers.get(AddAnotherPackagePage(houseConsignmentIndex, itemIndex)) match {
          case None        => form(viewModel, houseConsignmentIndex, itemIndex)
          case Some(value) => form(viewModel, houseConsignmentIndex, itemIndex).fill(value)
        }
        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, houseConsignmentMode: Mode, itemMode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider(request.userAnswers, arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
        form(viewModel, houseConsignmentIndex, itemIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, viewModel))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAnotherPackagePage(houseConsignmentIndex, itemIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield
                if (value) {
                  Redirect(
                    routes.PackageTypeController
                      .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, viewModel.nextIndex, houseConsignmentMode, itemMode, NormalMode)
                  )
                } else {
                  itemMode match {
                    case CheckMode =>
                      Redirect(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
                    case NormalMode =>
                      Redirect(
                        controllers.houseConsignment.index.items.routes.AddAnotherItemController
                          .onPageLoad(arrivalId, houseConsignmentIndex, houseConsignmentMode)
                      )
                  }
                }
          )
    }
}
