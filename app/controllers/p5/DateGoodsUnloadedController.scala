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

package controllers.p5

import controllers.actions._
import forms.DateGoodsUnloadedFormProvider
import models.{ArrivalId, Mode}
import navigation.Navigator
import pages.{DateGoodsUnloadedPage, DateOfPreparationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.p5.DateGoodsUnloadedView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateGoodsUnloadedController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  navigator: Navigator,
  formProvider: DateGoodsUnloadedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DateGoodsUnloadedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val form = formProvider(LocalDate.now.minusDays(6)) //todo update when IE043 message work complete
        val preparedForm = request.userAnswers.get(DateGoodsUnloadedPage) match {
          case Some(value) => form.fill(value)
          case None        => form
        }

        Ok(view(request.userAnswers.mrn, arrivalId, mode, preparedForm))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        formProvider(LocalDate.now.minusDays(6)) //todo update when IE043 message work complete
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
