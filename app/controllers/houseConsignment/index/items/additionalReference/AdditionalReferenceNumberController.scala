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

import controllers.actions._
import forms.ItemsAdditionalReferenceNumberFormProvider
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.AdditionalReferenceNavigator.AdditionalReferenceNavigatorProvider
import pages.houseConsignment.index.items.additionalReference.{AdditionalReferenceInCL234Page, AdditionalReferenceNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.additionalReference.AdditionalReferenceNumberViewModel.AdditionalReferenceNumberViewModelProvider
import views.html.houseConsignment.index.items.additionalReference.AdditionalReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: AdditionalReferenceNavigatorProvider,
  formProvider: ItemsAdditionalReferenceNumberFormProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalReferenceNumberView,
  viewModelProvider: AdditionalReferenceNumberViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    additionalReferenceMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    additionalReferenceIndex: Index
  ): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .andThen(getMandatoryPage(AdditionalReferenceInCL234Page(houseConsignmentIndex, itemIndex, additionalReferenceIndex))) {
        implicit request =>
          val viewModel = viewModelProvider.apply(
            arrivalId,
            houseConsignmentMode,
            itemMode,
            additionalReferenceMode,
            houseConsignmentIndex,
            itemIndex,
            additionalReferenceIndex
          )
          val form = formProvider(viewModel.requiredError, request.arg)
          val preparedForm = request.userAnswers.get(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, request.userAnswers.mrn, viewModel))
      }

  def onSubmit(
    arrivalId: ArrivalId,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    additionalReferenceMode: Mode,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    additionalReferenceIndex: Index
  ): Action[AnyContent] = actions
    .requireData(arrivalId)
    .andThen(getMandatoryPage(AdditionalReferenceInCL234Page(houseConsignmentIndex, itemIndex, additionalReferenceIndex)))
    .async {
      implicit request =>
        val viewModel = viewModelProvider.apply(
          arrivalId,
          houseConsignmentMode,
          itemMode,
          additionalReferenceMode,
          houseConsignmentIndex,
          itemIndex,
          additionalReferenceIndex
        )
        val form = formProvider(viewModel.requiredError, request.arg)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, viewModel))),
            value => redirect(value, houseConsignmentIndex, itemIndex, additionalReferenceIndex, houseConsignmentMode, itemMode, additionalReferenceMode)
          )
    }

  private def redirect(
    value: String,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    additionalReferenceIndex: Index,
    houseConsignmentMode: Mode,
    itemMode: Mode,
    additionalReferenceMode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(
        request.userAnswers.set(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), value)
      )
      _ <- sessionRepository.set(updatedAnswers)
    } yield {
      val navigator = navigatorProvider.apply(houseConsignmentMode, itemMode)
      Redirect(
        navigator.nextPage(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex),
                           additionalReferenceMode,
                           request.userAnswers
        )
      )
    }
}
