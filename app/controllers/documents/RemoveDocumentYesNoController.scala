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

package controllers.documents

import controllers.actions._
import forms.YesNoFormProvider
import models.removable.Document
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.sections.documents.DocumentSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.documents.RemoveDocumentYesNoView

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

  private val form: Form[Boolean] = formProvider("document.removeDocumentYesNo")

  private def addAnother(arrivalId: ArrivalId, mode: Mode): Call = controllers.documents.routes.AddAnotherDocumentController.onPageLoad(arrivalId, mode)

  private def formatInsetText(userAnswers: UserAnswers, documentIndex: Index): Option[String] =
    Document(userAnswers, documentIndex).map(_.forRemoveDisplay)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, DocumentSection(documentIndex), addAnother(arrivalId, mode)) {
      implicit request =>
        val insetText = formatInsetText(request.userAnswers, documentIndex)
        Ok(view(form, request.userAnswers.mrn, arrivalId, documentIndex, mode, insetText))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, DocumentSection(documentIndex), addAnother(arrivalId, mode))
    .async {
      implicit request =>
        val insetText = formatInsetText(request.userAnswers, documentIndex)
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, documentIndex, mode, insetText))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeDocument(DocumentSection(documentIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode))
          )
    }
}
