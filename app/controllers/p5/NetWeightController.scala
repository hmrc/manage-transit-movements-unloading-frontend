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
import forms.NetWeightFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.NetWeightPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.p5.NetWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NetWeightController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: NetWeightFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NetWeightView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(index: Index) = formProvider(index)

  def onPageLoad(arrivalId: ArrivalId, index: Index = Index(0), mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(NetWeightPage) match {
        case None        => form(index)
        case Some(value) => form(index).fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, index, mode))
  }

  def onSubmit(arrivalId: ArrivalId, index: Index = Index(0), mode: Mode): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      form(index)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, index, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(NetWeightPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(NetWeightPage, mode, updatedAnswers))
        )
  }
}
