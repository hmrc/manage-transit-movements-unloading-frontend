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

package controllers

import controllers.actions._
import forms.YesNoFormProvider
import models.{ArrivalId, Mode}
import navigation.Navigation
import pages.AddUnloadingCommentsYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AddUnloadingCommentsYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddUnloadingCommentsYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigation,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddUnloadingCommentsYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("addUnloadingCommentsYesNo")

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.getStatus(arrivalId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddUnloadingCommentsYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.getStatus(arrivalId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode))),
          value =>
            // TODO - if user selects No we need to wipe their user answers and re-transform the IE043
            //  we need to persist the answers to the pre-cross-check questions
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddUnloadingCommentsYesNoPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AddUnloadingCommentsYesNoPage, mode, updatedAnswers))
        )
  }
}
