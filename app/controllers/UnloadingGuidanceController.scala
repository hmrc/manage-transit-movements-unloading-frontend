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

          unloadingPermission
            .getMessageId(arrivalId, UnloadingPermission)
            .map {
              case Some(messageId) =>
                Ok(view(request.userAnswers.mrn, arrivalId, messageId, NormalMode, unloadingGuidanceViewModel.apply(newAuth, goodsTooLarge)))
              case None =>
                Redirect(controllers.routes.ErrorController.technicalDifficulties())
            }
      }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .andThen(getMandatoryPage(NewAuthYesNoPage)) {
        implicit request =>
          val newAuth: Boolean                                               = request.arg
          lazy val goodsTooLarge: Option[Boolean]                            = request.userAnswers.get(GoodsTooLargeForContainerYesNoPage)
          lazy val revisedUnloadingProcedureConditionsYesNo: Option[Boolean] = request.userAnswers.get(RevisedUnloadingProcedureConditionsYesNoPage)

          val redirectRoute = if (newAuth) {
            (goodsTooLarge, revisedUnloadingProcedureConditionsYesNo) match {
              case (_, Some(false)) => routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode)
              case (Some(true), _)  => routes.LargeUnsealedGoodsRecordDiscrepanciesYesNoController.onPageLoad(arrivalId, NormalMode)
              case (Some(false), _) => routes.PhotographExternalSealController.onPageLoad(arrivalId)
              case (None, _)        => routes.GoodsTooLargeForContainerYesNoController.onPageLoad(arrivalId, NormalMode)
            }
          } else {
            routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode)
          }

          Redirect(redirectRoute)
      }
}
