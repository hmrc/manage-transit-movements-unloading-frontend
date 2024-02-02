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

package controllers.houseConsignment.index.items

import controllers.actions._
import forms.NumberOfPackagesFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.NumberOfPackagesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.NumberOfPackagesViewModel.NumberOfPackagesViewModelProvider
import views.html.houseConsignment.index.items.NumberOfPackagesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NumberOfPackagesController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: NumberOfPackagesFormProvider,
  viewModelProvider: NumberOfPackagesViewModelProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NumberOfPackagesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(houseConsignmentIndex, itemIndex, mode)
        val form      = formProvider(viewModel.requiredError)
        val preparedForm = request.userAnswers.get(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(
          view(
            preparedForm,
            arrivalId,
            request.userAnswers.mrn,
            houseConsignmentIndex,
            itemIndex,
            packageIndex,
            mode,
            viewModel
          )
        )

    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider.apply(houseConsignmentIndex, itemIndex, mode)
        val form      = formProvider(viewModel.requiredError)
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    arrivalId,
                    request.userAnswers.mrn,
                    houseConsignmentIndex,
                    itemIndex,
                    packageIndex,
                    mode,
                    viewModel
                  )
                )
              ),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex), mode, updatedAnswers))
          )
    }
}
