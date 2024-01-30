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
import forms.VehicleIdentificationNumberFormProvider
import models.{ArrivalId, Index, Mode}
import pages.departureMeansOfTransport.VehicleIdentificationNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.departureMeansOfTransport.VehicleIdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VehicleIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: VehicleIdentificationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: VehicleIdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(arrivalId: ArrivalId, transportMeansIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId) {
      implicit request =>
        val preparedForm = request.userAnswers.get(VehicleIdentificationNumberPage(transportMeansIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, transportMeansIndex, mode))
    }

  def onSubmit(arrivalId: ArrivalId, transportMeansIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, transportMeansIndex, mode))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(VehicleIdentificationNumberPage(transportMeansIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
          )
    }
}