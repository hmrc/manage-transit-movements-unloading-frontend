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
import handlers.ErrorHandler
import models.{ArrivalId, UserAnswers}
import pages.TotalNumberOfItemsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TotalNumberOfItemsRejectionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TotalNumberOfItemsRejectionController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: TotalNumberOfItemsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  rejectionService: UnloadingRemarksRejectionService,
  val appConfig: FrontendAppConfig,
  errorHandler: ErrorHandler,
  view: TotalNumberOfItemsRejectionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = actions.getData(arrivalId).async {
    implicit request =>
      rejectionService.getRejectedValueAsInt(arrivalId, request.userAnswers)(TotalNumberOfItemsPage) flatMap {
        case Some(value) => Future.successful(Ok(view(form.fill(value), arrivalId)))
        case None        => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
      }
  }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] = actions.getData(arrivalId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, arrivalId))),
          value =>
            rejectionService.unloadingRemarksRejectionMessage(arrivalId) flatMap {
              case Some(rejectionMessage) =>
                val userAnswers = UserAnswers(arrivalId, rejectionMessage.movementReferenceNumber, request.eoriNumber)
                for {
                  updatedAnswers <- Future.fromTry(userAnswers.set(TotalNumberOfItemsPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId))

              case _ => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
            }
        )
  }
}
