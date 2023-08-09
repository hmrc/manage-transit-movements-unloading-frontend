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
import forms.DateGoodsUnloadedFormProvider
import models.{ArrivalId, Mode}
import navigation.Navigator
import pages.{DateGoodsUnloadedPage, PreparationDateAndTimePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DateGoodsUnloadedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateGoodsUnloadedController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: IE043DataRequiredActionProvider,
  navigator: Navigator,
  formProvider: DateGoodsUnloadedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DateGoodsUnloadedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    (actions.getStatus(arrivalId)
      andThen getMandatoryPage(PreparationDateAndTimePage)) {
      implicit request =>
        val form = formProvider(request.arg.toLocalDate)

        val preparedForm = request.userAnswers.get(DateGoodsUnloadedPage) match {
          case Some(value) => form.fill(value)
          case None        => form
        }

        Ok(view(request.userAnswers.mrn, arrivalId, mode, preparedForm))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    (actions.getStatus(arrivalId)
      andThen getMandatoryPage(PreparationDateAndTimePage)).async {
      implicit request =>
        formProvider(request.arg.toLocalDate)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(request.userAnswers.mrn, arrivalId, mode, formWithErrors))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(DateGoodsUnloadedPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(DateGoodsUnloadedPage, mode, updatedAnswers))
          )
    }
}
