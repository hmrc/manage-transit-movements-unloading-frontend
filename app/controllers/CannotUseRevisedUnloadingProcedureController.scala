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
import models.{ArrivalId, NormalMode, RichCC043CType}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CannotUseRevisedUnloadingProcedureView

import javax.inject.Inject

class CannotUseRevisedUnloadingProcedureController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: CannotUseRevisedUnloadingProcedureView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    actions
      .requireData(arrivalId) {
        implicit request =>
          Ok(view(request.userAnswers.mrn, arrivalId, NormalMode))
      }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        if (request.userAnswers.ie043Data.sealsExist) {
          Redirect(controllers.routes.CanSealsBeReadController.onPageLoad(arrivalId, NormalMode))
        } else {
          Redirect(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
    }
}
