/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.YesNoFormProvider
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.transformers.IE043Transformer
import views.html.RevisedUnloadingProcedureUnmetConditionsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RevisedUnloadingProcedureUnmetConditionsController @Inject() (
  override val messagesApi: MessagesApi,
  override val sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  formProvider: YesNoFormProvider,
  view: RevisedUnloadingProcedureUnmetConditionsView,
  override val dataTransformer: IE043Transformer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with NewAuthYesNoSubmission
    with I18nSupport {

  private val form = formProvider("newAuthYesNo")

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    actions
      .requireData(arrivalId) {
        implicit request =>
          Ok(view(request.userAnswers.mrn, arrivalId))
      }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        form
          .bind(Map("value" -> "false"))
          .fold(
            _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad())),
            value => submitNewAuth(request, value, _ => controllers.routes.UnloadingGuidanceController.onPageLoad(arrivalId))
          )
    }
}
