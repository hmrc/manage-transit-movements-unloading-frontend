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
import forms.EnumerableFormProvider
import models.{ArrivalId, Mode, UnloadingType}
import navigation.Navigator
import pages.UnloadingTypePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UnloadingTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UnloadingTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(mode: Mode) = formProvider[UnloadingType](mode, "unloadingType")

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId) {
      implicit request =>
        val preparedForm: Form[UnloadingType] = request.userAnswers.get(UnloadingTypePage) match {
          case None        => form(mode)
          case Some(value) => form(mode).fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, UnloadingType.values, arrivalId, mode))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        form(mode)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, UnloadingType.values, arrivalId, mode))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(UnloadingTypePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(UnloadingTypePage, mode, updatedAnswers))
          )
    }
}
