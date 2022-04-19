/*
 * Copyright 2022 HM Revenue & Customs
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

import audit.services.AuditEventSubmissionService
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{CheckArrivalStatusProvider, DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import handlers.ErrorHandler
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ReferenceDataService, UnloadingPermissionService, UnloadingRemarksService}
import uk.gov.hmrc.http.HttpErrorFunctions
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.CheckYourAnswersViewModel
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  unloadingPermissionService: UnloadingPermissionService,
  val controllerComponents: MessagesControllerComponents,
  val appConfig: FrontendAppConfig,
  referenceDataService: ReferenceDataService,
  errorHandler: ErrorHandler,
  unloadingRemarksService: UnloadingRemarksService,
  auditEventSubmissionService: AuditEventSubmissionService,
  checkArrivalStatus: CheckArrivalStatusProvider,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with HttpErrorFunctions {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        unloadingPermissionService.getUnloadingPermission(arrivalId).flatMap {
          case Some(unloadingPermission) =>
            referenceDataService.getCountryByCode(unloadingPermission.transportCountry).map {
              transportCountry =>
                val viewModel = CheckYourAnswersViewModel(request.userAnswers, unloadingPermission, transportCountry)
                Ok(view(request.userAnswers.mrn, arrivalId, viewModel.sections))
            }
          case _ => errorHandler.onClientError(request, BAD_REQUEST)
        }
    }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        unloadingPermissionService.getUnloadingPermission(arrivalId).flatMap {
          case Some(unloadingPermission) =>
            unloadingRemarksService.submit(arrivalId, request.userAnswers, unloadingPermission) flatMap {
              case Some(status) =>
                status match {
                  case status if is2xx(status) =>
                    auditEventSubmissionService.auditUnloadingRemarks(request.userAnswers, "submitUnloadingRemarks")
                    Future.successful(Redirect(routes.ConfirmationController.onPageLoad(arrivalId)))
                  case status if is4xx(status) => errorHandler.onClientError(request, status)
                  case _                       => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
                }
              case None => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
            }
          case _ => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
        }
    }

}
