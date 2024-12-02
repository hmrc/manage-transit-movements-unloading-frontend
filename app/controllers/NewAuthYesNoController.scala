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
import models.requests.DataRequest
import models.{ArrivalId, Mode, UserAnswers}
import navigation.Navigation
import pages.NewAuthYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.transformers.IE043Transformer
import views.html.NewAuthYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait NewAuthYesNoSubmission {
  val sessionRepository: SessionRepository
  val dataTransformer: IE043Transformer

  def submitNewAuth(request: DataRequest[AnyContent], formBindResult: Boolean, redirectCall: UserAnswers => Call)(implicit
    headerCarrier: HeaderCarrier,
    ec: ExecutionContext
  ) = {
    val userAnswersF: Future[UserAnswers] =
      if (request.userAnswers.hasAnswerChanged(NewAuthYesNoPage, formBindResult) && formBindResult) {
        request.userAnswers.wipeAndTransform(dataTransformer.transform(_))
      } else {
        Future.successful(request.userAnswers)
      }

    for {
      userAnswers    <- userAnswersF
      updatedAnswers <- Future.fromTry(userAnswers.set(NewAuthYesNoPage, formBindResult))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(redirectCall(updatedAnswers))
  }
}

class NewAuthYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  override val sessionRepository: SessionRepository,
  navigator: Navigation,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NewAuthYesNoView,
  override val dataTransformer: IE043Transformer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with NewAuthYesNoSubmission
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
          value => submitNewAuth(request, value, updatedAnswers => navigator.nextPage(NewAuthYesNoPage, mode, updatedAnswers))
        )
  }
}
