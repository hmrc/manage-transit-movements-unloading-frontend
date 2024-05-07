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

import controllers.actions._
import forms.YesNoFormProvider
import models.removable.Document
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.sections.houseConsignment.index.documents.DocumentSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.documents.RemoveDocumentYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveDocumentYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveDocumentYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, DocumentSection(houseConsignmentIndex, documentIndex), addAnother(arrivalId, mode, houseConsignmentIndex)) {
      implicit request =>
        val insetText = formatInsetText(request.userAnswers, houseConsignmentIndex, documentIndex)
        Ok(view(form(houseConsignmentIndex), request.userAnswers.mrn, arrivalId, houseConsignmentIndex, documentIndex, mode, insetText))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, DocumentSection(houseConsignmentIndex, documentIndex), addAnother(arrivalId, mode, houseConsignmentIndex))
    .async {
      implicit request =>
        val insetText = formatInsetText(request.userAnswers, houseConsignmentIndex, documentIndex)
        form(houseConsignmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(
                  BadRequest(
                    view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, documentIndex, mode, insetText)
                  )
                ),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeDocument(DocumentSection(houseConsignmentIndex, documentIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode, houseConsignmentIndex))
          )
    }

  def form(houseConsignmentIndex: Index): Form[Boolean] =
    formProvider("houseConsignment.index.documents.removeDocumentYesNo", houseConsignmentIndex.display)

  private def addAnother(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index): Call =
    controllers.houseConsignment.index.documents.routes.AddAnotherDocumentController.onPageLoad(arrivalId, houseConsignmentIndex, mode)

  private def formatInsetText(userAnswers: UserAnswers, houseConsignmentIndex: Index, documentIndex: Index): Option[String] =
    Document(userAnswers, houseConsignmentIndex, documentIndex).map(_.forRemoveDisplay)
}
