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

import controllers.actions.*
import forms.YesNoFormProvider
import models.{ArrivalId, Mode}
import navigation.Navigation
import pages.{DidUserChooseNewProcedurePage, NewAuthYesNoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UsersAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.NewAuthYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NewAuthYesNoController @Inject() (override val messagesApi: MessagesApi,
                                        navigator: Navigation,
                                        actions: Actions,
                                        formProvider: YesNoFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: NewAuthYesNoView,
                                        usersAnswersService: UsersAnswersService,
                                        sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("newAuthYesNo")

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(NewAuthYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode))),
          value =>
            for {
              // DidUserChooseNewProcedurePage is similar to NewAuthYesNoPage to determine if the user chooses new procedure
              // For navigation we need to know how the user journey starts
              // However we cannot use NewAuthYesNoPage for this purpose as in some cases it's updated programmatically
              updatedAnswers <- Future.fromTry(request.userAnswers.set(DidUserChooseNewProcedurePage, value))
              updatedAndTransformedAnswers <- usersAnswersService
                .updateConditionalAndWipe(page = NewAuthYesNoPage, value = value, updatedAnswers)
              _ <- sessionRepository.set(updatedAndTransformedAnswers)
            } yield Redirect(navigator.nextPage(NewAuthYesNoPage, mode, updatedAndTransformedAnswers))
        )
  }
}
