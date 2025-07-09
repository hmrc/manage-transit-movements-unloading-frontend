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

import controllers.actions.*
import forms.DocumentReferenceNumberFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.DocumentNavigator.DocumentNavigatorProvider
import pages.houseConsignment.index.items.document.DocumentReferenceNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.document.ItemsDocumentReferenceNumberViewModel
import viewModels.houseConsignment.index.items.document.ItemsDocumentReferenceNumberViewModel.ItemsDocumentReferenceNumberViewModelProvider
import views.html.houseConsignment.index.items.document.DocumentReferenceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DocumentReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  navigatorProvider: DocumentNavigatorProvider,
  formProvider: DocumentReferenceNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DocumentReferenceNumberView,
  viewModelProvider: ItemsDocumentReferenceNumberViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: ItemsDocumentReferenceNumberViewModel): Form[String] =
    formProvider("houseConsignment.index.items.document.referenceNumber", viewModel.requiredError)

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
        val preparedForm = request.userAnswers.get(DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex)) match {
          case None        => form(viewModel)
          case Some(value) => form(viewModel).fill(value)
        }

        Ok(
          view(
            preparedForm,
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
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          val viewModel = viewModelProvider.apply(documentMode, houseConsignmentIndex, itemIndex)
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
                      itemMode,
                      documentMode,
                      viewModel,
                      houseConsignmentIndex,
                      itemIndex,
                      documentIndex
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield {
                  val navigator = navigatorProvider.apply(houseConsignmentMode, itemMode)
                  Redirect(navigator.nextPage(DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex), documentMode, request.userAnswers))
                }
            )
      }
}
