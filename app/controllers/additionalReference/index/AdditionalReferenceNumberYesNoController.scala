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
import models.{ArrivalId, Index, Mode}
import navigation.AdditionalReferenceNavigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import pages.additionalReference.AdditionalReferenceNumberYesNoPage
import views.html.additionalReference.index.AdditionalReferenceNumberYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceNumberYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: AdditionalReferenceNavigator,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AdditionalReferenceNumberYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("additionalReference.index.additionalReferenceNumberYesNo")

  def onPageLoad(arrivalId: ArrivalId, additionalReferenceIndex: Index, mode: Mode): Action[AnyContent] = actions.getStatus(arrivalId) {

    implicit request =>
      val preparedForm = request.userAnswers.get(AdditionalReferenceNumberYesNoPage(additionalReferenceIndex)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, additionalReferenceIndex, mode))
  }

  def onSubmit(arrivalId: ArrivalId, additionalReferenceIndex: Index, mode: Mode): Action[AnyContent] = actions.getStatus(arrivalId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, additionalReferenceIndex, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AdditionalReferenceNumberYesNoPage(additionalReferenceIndex), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AdditionalReferenceNumberYesNoPage(additionalReferenceIndex), mode, updatedAnswers))
        )
  }
}
