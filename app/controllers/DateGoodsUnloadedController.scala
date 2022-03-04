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

import config.FrontendAppConfig
import controllers.actions._
import forms.DateGoodsUnloadedFormProvider
import models.{ArrivalId, Mode}
import navigation.NavigatorUnloadingPermission
import pages.DateGoodsUnloadedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.UnloadingPermissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{DateInput, NunjucksSupport}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateGoodsUnloadedController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: NavigatorUnloadingPermission,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: DateGoodsUnloadedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  unloadingPermissionService: UnloadingPermissionService,
  frontendAppConfig: FrontendAppConfig,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        unloadingPermissionService
          .getUnloadingPermission(arrivalId)
          .flatMap {
            case Some(up) =>
              val form = formProvider(up.dateOfPreparation)

              val preparedForm = request.userAnswers.get(DateGoodsUnloadedPage) match {
                case Some(value) => form.fill(value)
                case None        => form
              }

              val viewModel = DateInput.localDate(preparedForm("value"))

              val json = Json.obj(
                "form"        -> preparedForm,
                "mrn"         -> request.userAnswers.mrn,
                "date"        -> viewModel,
                "onSubmitUrl" -> routes.DateGoodsUnloadedController.onSubmit(arrivalId, mode).url
              )

              renderer.render("dateGoodsUnloaded.njk", json).map(Ok(_))
            case None =>
              val json = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)

              renderer.render("technicalDifficulties.njk", json).map(InternalServerError(_))
          }

    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        unloadingPermissionService
          .getUnloadingPermission(arrivalId)
          .flatMap {
            case Some(up) =>
              formProvider(up.dateOfPreparation)
                .bindFromRequest()
                .fold(
                  formWithErrors => {

                    val viewModel = DateInput.localDate(formWithErrors("value"))

                    val json = Json.obj(
                      "form"        -> formWithErrors,
                      "mrn"         -> request.userAnswers.mrn,
                      "date"        -> viewModel,
                      "onSubmitUrl" -> routes.DateGoodsUnloadedController.onSubmit(arrivalId, mode).url
                    )

                    renderer.render("dateGoodsUnloaded.njk", json).map(BadRequest(_))
                  },
                  value =>
                    for {
                      updatedAnswers      <- Future.fromTry(request.userAnswers.set(DateGoodsUnloadedPage, value))
                      _                   <- sessionRepository.set(updatedAnswers)
                      unloadingPermission <- unloadingPermissionService.getUnloadingPermission(arrivalId)
                    } yield Redirect(navigator.nextPage(DateGoodsUnloadedPage, mode, updatedAnswers, unloadingPermission))
                )

            case None =>
              val json = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)

              renderer.render("technicalDifficulties.njk", json).map(InternalServerError(_))
          }

    }
}
