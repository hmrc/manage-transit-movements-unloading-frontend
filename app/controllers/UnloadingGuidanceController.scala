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
import models.P5.ArrivalMessageType.UnloadingPermission
import models.{ArrivalId, NormalMode}
import pages.{GoodsTooLargeForContainerYesNoPage, NewAuthYesNoPage, RevisedUnloadingProcedureConditionsYesNoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.P5.UnloadingPermissionMessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.UnloadingGuidanceViewModel.UnloadingGuidanceViewModelProvider
import views.html.UnloadingGuidanceView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UnloadingGuidanceController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: UnloadingGuidanceView,
  unloadingPermission: UnloadingPermissionMessageService,
  viewModelProvider: UnloadingGuidanceViewModelProvider
)(implicit executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        unloadingPermission
          .getMessageId(arrivalId, UnloadingPermission)
          .map {
            case Some(messageId) =>
              Ok(view(request.userAnswers.mrn, arrivalId, messageId, NormalMode, viewModelProvider.apply(request.userAnswers)))
            case None =>
              Redirect(controllers.routes.ErrorController.technicalDifficulties())
          }
    }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val redirectRoute = (
          request.userAnswers.get(NewAuthYesNoPage),
          request.userAnswers.get(RevisedUnloadingProcedureConditionsYesNoPage),
          request.userAnswers.get(GoodsTooLargeForContainerYesNoPage)
        ) match {
          case (Some(false), _, _)                   => routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode)
          case (Some(true), Some(false), _)          => routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode)
          case (Some(true), Some(true), Some(true))  => routes.LargeUnsealedGoodsRecordDiscrepanciesYesNoController.onPageLoad(arrivalId, NormalMode)
          case (Some(true), Some(true), Some(false)) => routes.PhotographExternalSealController.onPageLoad(arrivalId)
          case _                                     => routes.NewAuthYesNoController.onPageLoad(arrivalId, NormalMode)
        }

        Redirect(redirectRoute)
    }
}
