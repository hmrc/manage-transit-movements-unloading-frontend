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
import javax.inject.Inject
import models.{ArrivalId, Index, NormalMode}
import pages.ChangesToReportPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import renderer.Renderer
import services.{ReferenceDataService, UnloadingPermissionService, UnloadingPermissionServiceImpl}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UnloadingSummaryHelper
import viewModels.{SealsSection, UnloadingSummaryViewModel}

import scala.concurrent.ExecutionContext

class UnloadingSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  unloadingPermissionService: UnloadingPermissionService,
  referenceDataService: ReferenceDataService,
  unloadingPermissionServiceImpl: UnloadingPermissionServiceImpl,
  errorHandler: ErrorHandler,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val redirectUrl: ArrivalId => Call =
    arrivalId => controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId)

  private val addCommentUrl: ArrivalId => Call =
    arrivalId => controllers.routes.ChangesToReportController.onPageLoad(arrivalId, NormalMode)

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        unloadingPermissionService.getUnloadingPermission(arrivalId).flatMap {
          case Some(unloadingPermission) =>
            //TODO: Move unloading summary into UnloadingSummaryViewModel
            val unloadingSummaryRow: UnloadingSummaryHelper = new UnloadingSummaryHelper(request.userAnswers)
            val sealsSection                                = SealsSection(request.userAnswers)(unloadingPermission, unloadingSummaryRow)

            val numberOfSeals = request.userAnswers.get(DeriveNumberOfSeals) match {
              case Some(sealsNum) => sealsNum
              case None =>
                unloadingPermissionServiceImpl.convertSeals(request.userAnswers, unloadingPermission) match {
                  case Some(ua) => ua.get(DeriveNumberOfSeals).getOrElse(0)
                  case _        => 0
                }
            }

            val addSealUrl = controllers.routes.NewSealNumberController.onPageLoad(arrivalId, Index(numberOfSeals), NormalMode) //todo add mode

            referenceDataService.getCountryByCode(unloadingPermission.transportCountry).flatMap {
              transportCountry =>
                val sections = UnloadingSummaryViewModel(request.userAnswers, transportCountry)(unloadingPermission).sections

                val json =
                  Json.obj(
                    "mrn"                -> request.userAnswers.mrn,
                    "arrivalId"          -> arrivalId,
                    "redirectUrl"        -> redirectUrl(arrivalId).url,
                    "showAddCommentLink" -> request.userAnswers.get(ChangesToReportPage).isEmpty,
                    "addCommentUrl"      -> addCommentUrl(arrivalId).url,
                    "addSealUrl"         -> addSealUrl.url,
                    "sealsSection"       -> Json.toJson(sealsSection),
                    "sections"           -> Json.toJson(sections)
                  )

                renderer.render("unloadingSummary.njk", json).map(Ok(_))
            }
          case _ =>
            errorHandler.onClientError(request, BAD_REQUEST, "errors.malformedSeals") //todo: get design and content to look at this

        }
    }

}
