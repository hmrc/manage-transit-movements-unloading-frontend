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
import javax.inject.Inject
import models.{ArrivalId, Mode}
import navigation.Navigator
import pages.UnloadingGuidancePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext

class UnloadingGuidanceController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  navigator: Navigator,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        val pdfUrl = routes.UnloadingPermissionPDFController.getPDF(arrivalId).url

        val json = Json.obj(
          "mrn"         -> request.userAnswers.mrn,
          "nextPageUrl" -> navigator.nextPage(UnloadingGuidancePage, mode, request.userAnswers).url,
          "arrivalId"   -> arrivalId,
          "mode"        -> mode,
          "pdfUrl"      -> pdfUrl
        )

        renderer.render("unloadingGuidance.njk", json).map(Ok(_))
    }
}
