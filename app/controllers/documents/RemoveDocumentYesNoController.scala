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
import models.reference.DocumentType
import models.{ArrivalId, Index, Mode}
import pages.documents.{DocumentReferenceNumberPage, TypePage}
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
  view: RemoveDocumentYesNoView,
  getMandatoryPage: SpecificDataRequiredActionProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[Boolean] = formProvider("document.removeDocumentYesNo")

  // TODO: change once other page is ready
  private def addAnother(arrivalId: ArrivalId, mode: Mode): Call = new Call("GET", "/foo")

  private def formatInsetText(documentType: DocumentType, documentReferenceNumber: String): String =
    s"${documentType.`type`} - $documentReferenceNumber"

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, DocumentSection(documentIndex), addAnother(arrivalId, mode))
    .andThen(getMandatoryPage.getFirst(TypePage(documentIndex)))
    .andThen(getMandatoryPage.getSecond(DocumentReferenceNumberPage(documentIndex))) {
      implicit request =>
        val documentType: Option[DocumentType] = request.userAnswers.get(TypePage(documentIndex))
        val documentReference: Option[String]  = request.userAnswers.get(DocumentReferenceNumberPage(documentIndex))

        val insetText = formatInsetText(documentType.get, documentReference.get)
        Ok(view(form, request.userAnswers.mrn, arrivalId, documentIndex, mode, insetText))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, DocumentSection(documentIndex), addAnother(arrivalId, mode))
    .andThen(getMandatoryPage.getFirst(TypePage(documentIndex)))
    .andThen(getMandatoryPage.getSecond(DocumentReferenceNumberPage(documentIndex)))
    .async {
      implicit request =>
        val documentType: Option[DocumentType] = request.userAnswers.get(TypePage(documentIndex));
        val documentReference: Option[String]  = request.userAnswers.get(DocumentReferenceNumberPage(documentIndex));

        val insetText = formatInsetText(documentType.get, documentReference.get)
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
                    Future.fromTry(request.userAnswers.removeExceptSequenceNumber(DocumentSection(documentIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode))
          )
    }
}
