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
import forms.YesNoFormProvider
import models.reference.DocumentType
import models.{ArrivalId, Index, Mode, NormalMode, UserAnswers}
import pages.houseConsignment.index.items.document.{DocumentReferenceNumberPage, TypePage}
import pages.sections.houseConsignment.index.items.documents.DocumentSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.document.RemoveDocumentYesNoView

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

  def form(houseConsignmentIndex: Index, itemIndex: Index): Form[Boolean] =
    formProvider("houseConsignment.index.items.document.removeDocumentYesNo", houseConsignmentIndex.display, itemIndex.display)

  private def addAnother(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Call = Call(
    "GET",
    controllers.houseConsignment.index.items.document.routes.AddAnotherDocumentController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, mode).url
  )

  private def formatInsetText(userAnswers: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index): String = {
    val documentType: DocumentType      = userAnswers.get(TypePage(houseConsignmentIndex, itemIndex, documentIndex)).get
    val documentReferenceNumber: String = userAnswers.get(DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex)).get

    s"${documentType.`type`} - $documentReferenceNumber"
  }

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, DocumentSection(houseConsignmentIndex, itemIndex, documentIndex), addAnother(arrivalId, mode, houseConsignmentIndex, itemIndex))
    .andThen(getMandatoryPage.getFirst(TypePage(houseConsignmentIndex, itemIndex, documentIndex)))
    .andThen(getMandatoryPage.getSecond(DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex))) {
      implicit request =>
        val insetText = formatInsetText(request.userAnswers, houseConsignmentIndex, itemIndex, documentIndex)
        Ok(view(form(houseConsignmentIndex, itemIndex), request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, documentIndex, mode, insetText))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, DocumentSection(houseConsignmentIndex, itemIndex, documentIndex), addAnother(arrivalId, mode, houseConsignmentIndex, itemIndex))
    .andThen(getMandatoryPage.getFirst(TypePage(houseConsignmentIndex, itemIndex, documentIndex)))
    .andThen(getMandatoryPage.getSecond(DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex)))
    .async {
      implicit request =>
        val insetText = formatInsetText(request.userAnswers, houseConsignmentIndex, itemIndex, documentIndex)
        form(houseConsignmentIndex, itemIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(
                  BadRequest(
                    view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex: Index, itemIndex: Index, documentIndex, mode, insetText)
                  )
                ),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeExceptSequenceNumber(DocumentSection(houseConsignmentIndex, itemIndex, documentIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode, houseConsignmentIndex, itemIndex))
          )
    }
}
