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

import com.google.inject.Inject
import controllers.actions.{Actions, SpecificDataRequiredActionProvider}
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.UnloadingFindingsViewModel.UnloadingFindingsViewModelProvider
import views.html.UnloadingFindingsView

class UnloadingFindingsController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: UnloadingFindingsView,
  viewModelProvider: UnloadingFindingsViewModelProvider
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val unloadingFindingsViewModel = viewModelProvider.apply(request.userAnswers)

      Ok(view(request.userAnswers.mrn, arrivalId, unloadingFindingsViewModel))
  }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId) {

    Redirect(controllers.routes.SessionExpiredController.onPageLoad()) //todo redirect to CYA when built
  }
}
