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
import forms.TotalNumberOfItemsFormProvider
import javax.inject.Inject
import models.{ArrivalId, UserAnswers}
import pages.TotalNumberOfItemsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class TotalNumberOfItemsRejectionController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  formProvider: TotalNumberOfItemsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  rejectionService: UnloadingRemarksRejectionService,
  val renderer: Renderer,
  val appConfig: FrontendAppConfig,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with TechnicalDifficultiesPage {

  private val form = formProvider()

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId)).async {
    implicit request =>
      rejectionService.getRejectedValueAsInt(arrivalId, request.userAnswers)(TotalNumberOfItemsPage) flatMap {
        case Some(originalAttrValue) =>
          val json = Json.obj(
            "form"        -> form.fill(originalAttrValue),
            "onSubmitUrl" -> routes.TotalNumberOfItemsRejectionController.onSubmit(arrivalId).url
          )
          renderer.render("totalNumberOfItems.njk", json).map(Ok(_))
        case None => renderTechnicalDifficultiesPage
      }
  }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] = (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId)).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val json = Json.obj(
              "form"        -> formWithErrors,
              "onSubmitUrl" -> routes.TotalNumberOfItemsRejectionController.onSubmit(arrivalId).url
            )

            renderer.render("totalNumberOfItems.njk", json).map(BadRequest(_))
          },
          value =>
            rejectionService.unloadingRemarksRejectionMessage(arrivalId) flatMap {
              case Some(rejectionMessage) =>
                val userAnswers = UserAnswers(arrivalId, rejectionMessage.movementReferenceNumber, request.eoriNumber)
                for {
                  updatedAnswers <- Future.fromTry(userAnswers.set(TotalNumberOfItemsPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId))

              case _ => renderTechnicalDifficultiesPage
            }
        )
  }
}
