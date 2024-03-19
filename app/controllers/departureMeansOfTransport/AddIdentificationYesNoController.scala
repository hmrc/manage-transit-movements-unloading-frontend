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

package controllers.departureMeansOfTransport

import controllers.actions._
import forms.YesNoFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.DepartureTransportMeansNavigator
import pages.departureMeansOfTransport.AddIdentificationYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.departureMeansOfTransport.AddIdentificationYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddIdentificationYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: DepartureTransportMeansNavigator,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddIdentificationYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("departureMeansOfTransport.addIdentificationYesNo")

  def onPageLoad(arrivalId: ArrivalId, transportMeansIndex: Index, mode: Mode): Action[AnyContent] = actions.getStatus(arrivalId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddIdentificationYesNoPage(transportMeansIndex)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, transportMeansIndex, mode))
  }

  def onSubmit(arrivalId: ArrivalId, transportMeansIndex: Index, mode: Mode): Action[AnyContent] = actions.getStatus(arrivalId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, transportMeansIndex, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddIdentificationYesNoPage(transportMeansIndex), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AddIdentificationYesNoPage(transportMeansIndex), mode, updatedAnswers))
        )
  }
}
