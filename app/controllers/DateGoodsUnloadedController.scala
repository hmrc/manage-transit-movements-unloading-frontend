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
import models.{ArrivalId, Mode, RichCC043CType}
import navigation.Navigation
import pages.DateGoodsUnloadedPage
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
  navigator: Navigation,
  formProvider: DateGoodsUnloadedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DateGoodsUnloadedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions
    .requireData(arrivalId) {
      implicit request =>
        val form = formProvider(request.userAnswers.ie043Data.preparationDateAndTime)

        val preparedForm = request.userAnswers.get(DateGoodsUnloadedPage) match {
          case Some(value) => form.fill(value)
          case None        => form
        }

        Ok(view(request.userAnswers.mrn, arrivalId, mode, preparedForm))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions
    .requireData(arrivalId)
    .async {
      implicit request =>
        formProvider(request.userAnswers.ie043Data.preparationDateAndTime)
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
