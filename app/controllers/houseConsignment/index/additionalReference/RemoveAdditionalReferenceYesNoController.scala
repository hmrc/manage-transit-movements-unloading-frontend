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

package controllers.houseConsignment.index.additionalReference

import controllers.actions._
import forms.YesNoFormProvider
import models.removable.AdditionalReference
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.sections.houseConsignment.index.additionalReference.AdditionalReferenceSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.additionalReference.RemoveAdditionalReferenceYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveAdditionalReferenceYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAdditionalReferenceYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] =
    formProvider("houseConsignment.index.additionalReference.removeAdditionalReferenceYesNo")

  private def addAnother(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index): Call =
    Call("GET", "#") //TODO redirect to addAnotherController

  def insetText(userAnswers: UserAnswers, houseConsignmentIndex: Index, additionalReferenceIndex: Index): Option[String] =
    AdditionalReference(userAnswers, houseConsignmentIndex, additionalReferenceIndex).map(_.forRemoveDisplay)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, additionalReferenceIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, AdditionalReferenceSection(houseConsignmentIndex, additionalReferenceIndex), addAnother(arrivalId, mode, houseConsignmentIndex)) {
      implicit request =>
        Ok(
          view(
            form,
            request.userAnswers.mrn,
            arrivalId,
            mode,
            houseConsignmentIndex,
            additionalReferenceIndex,
            insetText(request.userAnswers, houseConsignmentIndex, additionalReferenceIndex)
          )
        )
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, additionalReferenceIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, AdditionalReferenceSection(houseConsignmentIndex, additionalReferenceIndex), addAnother(arrivalId, mode, houseConsignmentIndex))
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      request.userAnswers.mrn,
                      arrivalId,
                      mode,
                      houseConsignmentIndex,
                      additionalReferenceIndex,
                      insetText(request.userAnswers, houseConsignmentIndex, additionalReferenceIndex)
                    )
                  )
                ),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeExceptSequenceNumber(AdditionalReferenceSection(houseConsignmentIndex, additionalReferenceIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode, houseConsignmentIndex))
          )
    }
}
