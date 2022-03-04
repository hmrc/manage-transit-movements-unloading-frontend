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

import cats.data.OptionT
import config.FrontendAppConfig
import controllers.actions._
import forms.DateGoodsUnloadedFormProvider
import models.{ArrivalId, UserAnswers}
import navigation.NavigatorUnloadingPermission
import pages.DateGoodsUnloadedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.{UnloadingPermissionService, UnloadingRemarksRejectionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{DateInput, NunjucksSupport}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateGoodsUnloadedRejectionController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: NavigatorUnloadingPermission,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  formProvider: DateGoodsUnloadedFormProvider,
  rejectionService: UnloadingRemarksRejectionService,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  unloadingPermissionService: UnloadingPermissionService,
  frontendAppConfig: FrontendAppConfig,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId)).async {
    implicit request =>
      (for {
        up            <- OptionT(unloadingPermissionService.getUnloadingPermission(arrivalId))
        originalValue <- OptionT(rejectionService.getRejectedValueAsDate(arrivalId, request.userAnswers)(DateGoodsUnloadedPage))
        dateOfPreparation = up.dateOfPreparation
      } yield {
        val form = formProvider(dateOfPreparation)

        val preparedForm = form.fill(originalValue)
        val viewModel    = DateInput.localDate(preparedForm("value"))

        Json.obj(
          "form"        -> preparedForm,
          "date"        -> viewModel,
          "onSubmitUrl" -> routes.DateGoodsUnloadedRejectionController.onSubmit(arrivalId).url
        )

      }).foldF {
        val json = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)

        renderer.render("technicalDifficulties.njk", json).map(InternalServerError(_))
      }(
        json => renderer.render("dateGoodsUnloaded.njk", json).map(Ok(_))
      )

  }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] = (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId)).async {
    implicit request =>
      (for {
        up <- OptionT(unloadingPermissionService.getUnloadingPermission(arrivalId))
        dateOfPreparation = up.dateOfPreparation
        rejectionMessage <- OptionT(rejectionService.unloadingRemarksRejectionMessage(arrivalId))
      } yield formProvider(dateOfPreparation)
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val viewModel = DateInput.localDate(formWithErrors("value"))

            val json = Json.obj(
              "form"        -> formWithErrors,
              "date"        -> viewModel,
              "onSubmitUrl" -> routes.DateGoodsUnloadedRejectionController.onSubmit(arrivalId).url
            )

            renderer.render("dateGoodsUnloaded.njk", json).map(BadRequest(_))
          },
          value => {
            val userAnswers = UserAnswers(arrivalId, rejectionMessage.movementReferenceNumber, request.eoriNumber)
            for {
              updatedAnswers <- Future.fromTry(userAnswers.set(DateGoodsUnloadedPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId))

          }
        )).getOrElse {
        val json = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)

        renderer.render("technicalDifficulties.njk", json).map(InternalServerError(_))
      }.flatten

  }

}
