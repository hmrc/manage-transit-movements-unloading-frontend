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
import pages.GoodsTooLargeForContainerYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.GoodsTooLargeForContainerYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsTooLargeForContainerYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigation,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: GoodsTooLargeForContainerYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("goodsTooLargeForContainerYesNo")

  def onPageLoad(arrivalId: ArrivalId, messageId: String, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(GoodsTooLargeForContainerYesNoPage(messageId)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, messageId, mode))
  }

  def onSubmit(arrivalId: ArrivalId, messageId: String, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, messageId, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(GoodsTooLargeForContainerYesNoPage(messageId), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(GoodsTooLargeForContainerYesNoPage(messageId), mode, updatedAnswers))
        )
  }
}
