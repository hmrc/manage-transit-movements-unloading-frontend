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

import controllers.actions._
import derivable.DeriveNumberOfSeals
import handlers.ErrorHandler
import models.requests.DataRequest
import models.{ArrivalId, UnloadingPermission}
import pages.ChangesToReportPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ReferenceDataService, UnloadingPermissionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.UnloadingSummaryViewModel
import views.html.UnloadingSummaryView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UnloadingSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  unloadingPermissionService: UnloadingPermissionService,
  referenceDataService: ReferenceDataService,
  errorHandler: ErrorHandler,
  checkArrivalStatus: CheckArrivalStatusProvider,
  view: UnloadingSummaryView,
  viewModel: UnloadingSummaryViewModel
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        unloadingPermissionService.getUnloadingPermission(arrivalId).flatMap {
          case Some(unloadingPermission) =>
            referenceDataService.getCountryByCode(unloadingPermission.transportCountry).map {
              transportCountry =>
                Ok(
                  view(
                    mrn = request.userAnswers.mrn,
                    arrivalId = arrivalId,
                    sealsSection = viewModel.sealsSection(request.userAnswers, unloadingPermission),
                    transportAndItemSections = viewModel.transportAndItemSections(request.userAnswers, transportCountry, unloadingPermission),
                    numberOfSeals = numberOfSeals(unloadingPermission),
                    showAddCommentLink = request.userAnswers.get(ChangesToReportPage).isEmpty
                  )
                )
            }
          case _ =>
            errorHandler.onClientError(request, BAD_REQUEST, "errors.malformedSeals") //todo: get design and content to look at this
        }
    }

  private def numberOfSeals(unloadingPermission: UnloadingPermission)(implicit request: DataRequest[_]): Int =
    request.userAnswers.get(DeriveNumberOfSeals) match {
      case Some(value) => value
      case None =>
        unloadingPermissionService.convertSeals(request.userAnswers, unloadingPermission) match {
          case Some(ua) => ua.get(DeriveNumberOfSeals).getOrElse(0)
          case _        => 0
        }
    }
}
