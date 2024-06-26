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

package controllers.additionalReference.index

import controllers.actions._
import forms.YesNoFormProvider
import models.removable.AdditionalReference
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.sections.additionalReference.AdditionalReferenceSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.additionalReference.index.RemoveAdditionalReferenceYesNoView

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

  def form(additionalReferenceIndex: Index): Form[Boolean] =
    formProvider("additionalReference.index.removeAdditionalReferenceYesNo", additionalReferenceIndex.display)

  private def addAnother(arrivalId: ArrivalId, mode: Mode): Call =
    controllers.additionalReference.index.routes.AddAnotherAdditionalReferenceController.onSubmit(arrivalId, mode)

  def insetText(userAnswers: UserAnswers, additionalReferenceIndex: Index): Option[String] =
    AdditionalReference(userAnswers, additionalReferenceIndex).map(_.forRemoveDisplay)

  def onPageLoad(arrivalId: ArrivalId, additionalReferenceIndex: Index, mode: Mode): Action[AnyContent] = actions
    .requireIndex(arrivalId, AdditionalReferenceSection(additionalReferenceIndex), addAnother(arrivalId, mode)) {
      implicit request =>
        Ok(
          view(
            form(additionalReferenceIndex),
            request.userAnswers.mrn,
            arrivalId,
            additionalReferenceIndex,
            insetText(request.userAnswers, additionalReferenceIndex),
            mode
          )
        )
    }

  def onSubmit(arrivalId: ArrivalId, additionalReferenceIndex: Index, mode: Mode): Action[AnyContent] = actions
    .requireIndex(arrivalId, AdditionalReferenceSection(additionalReferenceIndex), addAnother(arrivalId, mode))
    .async {
      implicit request =>
        form(additionalReferenceIndex)
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
                      additionalReferenceIndex,
                      insetText(request.userAnswers, additionalReferenceIndex),
                      mode
                    )
                  )
                ),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeDataGroup(AdditionalReferenceSection(additionalReferenceIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode))
          )
    }
}
