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

package controllers.houseConsignment.index.documents

import controllers.actions.*
import forms.DocumentReferenceNumberFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.HouseConsignmentDocumentNavigator.HouseConsignmentDocumentNavigatorProvider
import pages.houseConsignment.index.documents.DocumentReferenceNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.documents.ReferenceNumberViewModel
import viewModels.houseConsignment.index.documents.ReferenceNumberViewModel.ReferenceNumberViewModelProvider
import views.html.houseConsignment.index.documents.ReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  navigatorProvider: HouseConsignmentDocumentNavigatorProvider,
  formProvider: DocumentReferenceNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ReferenceNumberView,
  viewModelProvider: ReferenceNumberViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: ReferenceNumberViewModel): Form[String] =
    formProvider("houseConsignment.index.documents.referenceNumber", viewModel.requiredError)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentMode: Mode, documentMode: Mode, houseConsignmentIndex: Index, documentIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(documentMode, houseConsignmentIndex)
        val preparedForm = request.userAnswers.get(DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex)) match {
          case None        => form(viewModel)
          case Some(value) => form(viewModel).fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentMode, documentMode, viewModel, houseConsignmentIndex, documentIndex))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentMode: Mode, documentMode: Mode, houseConsignmentIndex: Index, documentIndex: Index): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          val viewModel = viewModelProvider.apply(documentMode, houseConsignmentIndex)
          form(viewModel)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      request.userAnswers.mrn,
                      arrivalId,
                      houseConsignmentMode,
                      documentMode,
                      viewModel,
                      houseConsignmentIndex,
                      documentIndex
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield {
                  val navigator = navigatorProvider.apply(houseConsignmentMode)
                  Redirect(navigator.nextPage(DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex), documentMode, request.userAnswers))
                }
            )
      }
}
