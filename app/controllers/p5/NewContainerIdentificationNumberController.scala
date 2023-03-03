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
import forms.NewContainerIdentificationNumberFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.ContainerIdentificationNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.p5.NewContainerIdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NewContainerIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: NewContainerIdentificationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NewContainerIdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  //TODO: Do not allow duplicate container identification numbers to be submitted

  def onPageLoad(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ContainerIdentificationNumberPage(index)) match {
        case None                       => form
        case Some(identificationNumber) => form.fill(identificationNumber)
      }

      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, index, mode))
  }

  def onSubmit(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, index, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ContainerIdentificationNumberPage(index), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(controllers.routes.SessionExpiredController.onPageLoad()) // TODO: Implement once navigation is in
        )

  }

}