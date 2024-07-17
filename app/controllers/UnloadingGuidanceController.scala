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
import generated.CC043CType
import models.P5.ArrivalMessageType.UnloadingPermission
import models.{ArrivalId, NormalMode}
import pages.{GoodsTooLargeForContainerYesNoPage, NewAuthYesNoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.P5.UnloadingPermissionMessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.UnloadingGuidanceViewModel.UnloadingGuidanceViewModelProvider
import views.html.UnloadingGuidanceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingGuidanceController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: UnloadingGuidanceView,
  unloadingPermission: UnloadingPermissionMessageService,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  unloadingGuidanceViewModel: UnloadingGuidanceViewModelProvider
)(implicit executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .andThen(getMandatoryPage(NewAuthYesNoPage))
      .async {
        implicit request =>
          val newAuth: Boolean               = request.arg
          val goodsTooLarge: Option[Boolean] = request.userAnswers.get(GoodsTooLargeForContainerYesNoPage)

          val message = unloadingPermission.getMessageId(arrivalId, UnloadingPermission)

          for {
            messageId <- message
          } yield messageId match {
            case Some(id) =>
              Ok(view(request.userAnswers.mrn, arrivalId, id, NormalMode, unloadingGuidanceViewModel.apply(newAuth, goodsTooLarge)))
            case None => Redirect(controllers.routes.ErrorController.technicalDifficulties())
          }

      }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] =
    actions.requireData(arrivalId) {
      _ => Redirect(routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode))
    }
}
