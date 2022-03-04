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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.{ReferenceDataService, UnloadingPermissionService, UnloadingRemarksService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.CheckYourAnswersViewModel
import viewModels.sections.Section

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  unloadingPermissionService: UnloadingPermissionService,
  val controllerComponents: MessagesControllerComponents,
  val renderer: Renderer,
  val appConfig: FrontendAppConfig,
  referenceDataService: ReferenceDataService,
  errorHandler: ErrorHandler,
  unloadingRemarksService: UnloadingRemarksService,
  auditEventSubmissionService: AuditEventSubmissionService,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with TechnicalDifficultiesPage {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        unloadingPermissionService.getUnloadingPermission(arrivalId).flatMap {
          case Some(unloadingPermission) =>
            referenceDataService.getCountryByCode(unloadingPermission.transportCountry).flatMap {
              transportCountry =>
                val viewModel = CheckYourAnswersViewModel(request.userAnswers, unloadingPermission, transportCountry)

                val answers: Seq[Section] = viewModel.sections

                renderer
                  .render(
                    "check-your-answers.njk",
                    Json.obj(
                      "mrn"       -> request.userAnswers.mrn,
                      "arrivalId" -> arrivalId,
                      "sections"  -> Json.toJson(answers)
                    )
                  )
                  .map(Ok(_))
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
                  case ACCEPTED =>
                    auditEventSubmissionService.auditUnloadingRemarks(request.userAnswers, "submitUnloadingRemarks")
                    Future.successful(Redirect(routes.ConfirmationController.onPageLoad(arrivalId)))
                  case UNAUTHORIZED => errorHandler.onClientError(request, UNAUTHORIZED)
                  case _            => renderTechnicalDifficultiesPage
                }

              case None => renderTechnicalDifficultiesPage
            }
          case _ => renderTechnicalDifficultiesPage
        }
    }

}
