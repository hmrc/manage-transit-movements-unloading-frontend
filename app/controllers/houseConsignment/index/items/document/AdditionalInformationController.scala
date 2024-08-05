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

package controllers.houseConsignment.index.items.document

import controllers.actions._
import forms.ItemsAdditionalInformationFormProvider
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.DocumentNavigator.DocumentNavigatorProvider
import pages.houseConsignment.index.items.document.AdditionalInformationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.document.ItemsAdditionalInformationViewModel.ItemsAdditionalInformationViewModelProvider
import views.html.houseConsignment.index.items.document.AdditionalInformationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  formProvider: ItemsAdditionalInformationFormProvider,
  navigatorProvider: DocumentNavigatorProvider,
  view: AdditionalInformationView,
  viewModelProvider: ItemsAdditionalInformationViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    documentMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index
  ): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(documentMode, houseConsignmentIndex, itemIndex)
        val form      = formProvider(viewModel.requiredError)
        val preparedForm = request.userAnswers.get(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(
          view(preparedForm,
               request.userAnswers.mrn,
               arrivalId,
               houseConsignmentMode,
               itemMode,
               documentMode,
               viewModel,
               houseConsignmentIndex,
               itemIndex,
               documentIndex
          )
        )
    }

  def onSubmit(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    documentMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index
  ): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider.apply(documentMode, houseConsignmentIndex, itemIndex)
        formProvider(viewModel.requiredError)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(formWithErrors,
                       request.userAnswers.mrn,
                       arrivalId,
                       houseConsignmentMode,
                       itemMode,
                       documentMode,
                       viewModel,
                       houseConsignmentIndex,
                       itemIndex,
                       documentIndex
                  )
                )
              ),
            value => redirect(houseConsignmentMode, itemMode, documentMode, value, houseConsignmentIndex, itemIndex, documentIndex)
          )
    }

  private def redirect(
    houseConsignmentMode: Mode,
    itemMode: Mode,
    documentMode: Mode,
    value: String,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    documentIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield {
      val navigator = navigatorProvider.apply(houseConsignmentMode, itemMode)
      Redirect(navigator.nextPage(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, request.userAnswers))
    }
}
