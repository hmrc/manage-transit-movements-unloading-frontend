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
import models.{ArrivalId, NormalMode}
import pages.{GoodsTooLargeForContainerYesNoPage, NewAuthYesNoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.UnloadingGuidanceViewModel.UnloadingGuidanceViewModelProvider
import views.html.UnloadingGuidanceView

import javax.inject.Inject

class UnloadingGuidanceController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: UnloadingGuidanceView,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  unloadingGuidanceViewModel: UnloadingGuidanceViewModelProvider
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, messageId: String): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .andThen(getMandatoryPage(NewAuthYesNoPage(messageId))) {
        implicit request =>
          val newAuth: Boolean               = request.arg
          val goodsTooLarge: Option[Boolean] = request.userAnswers.get(GoodsTooLargeForContainerYesNoPage(messageId))
          Ok(view(request.userAnswers.mrn, arrivalId, messageId, NormalMode, unloadingGuidanceViewModel.apply(newAuth, goodsTooLarge)))
      }

  def onSubmit(arrivalId: ArrivalId, messageId: String): Action[AnyContent] =
    actions.requireData(arrivalId) {
      _ => Redirect(routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode))
    }
}
