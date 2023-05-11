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
import controllers.actions.Actions
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.UnloadingFindingsViewModel
import viewModels.UnloadingFindingsViewModel.UnloadingFindingsViewModelProvider
import views.html.UnloadingFindingsView

import scala.concurrent.{ExecutionContext, Future}

class UnloadingFindingsController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: UnloadingFindingsView,
  viewModelProvider: UnloadingFindingsViewModelProvider
)(implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      val unloadingFindingsViewModel: Future[UnloadingFindingsViewModel] =
        viewModelProvider.apply(request.userAnswers)

      unloadingFindingsViewModel.map {
        viewModel =>
          Ok(view(request.userAnswers.mrn, arrivalId, viewModel))
      }

  }

}
