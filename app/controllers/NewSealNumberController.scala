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
import forms.NewSealNumberFormProvider
import models.{ArrivalId, Index, Mode, Seal}
import navigation.Navigator
import pages.SealPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.NewSealNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NewSealNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: NewSealNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NewSealNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  //TODO: Do not allow duplicate seal numbers to be submitted
  def onPageLoad(arrivalId: ArrivalId, equipmentIndex: Index, sealIndex: Index, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(SealPage(equipmentIndex, sealIndex)) match {
        case None       => form
        case Some(seal) => form.fill(seal)
      }

      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, equipmentIndex, sealIndex, mode))
  }

  def onSubmit(arrivalId: ArrivalId, equipmentIndex: Index, sealIndex: Index, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, equipmentIndex, sealIndex, mode))),
          value => {
            Future.successful(BadRequest(view(form, request.userAnswers.mrn, arrivalId, equipmentIndex, sealIndex, mode)))
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SealPage(equipmentIndex, sealIndex), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
          }
        )

  }

}
