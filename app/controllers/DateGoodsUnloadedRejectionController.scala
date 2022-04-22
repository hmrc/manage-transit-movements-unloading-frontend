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
import forms.DateGoodsUnloadedFormProvider
import handlers.ErrorHandler
import models.ArrivalId
import pages.DateGoodsUnloadedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UnloadingPermissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DateGoodsUnloadedRejectionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateGoodsUnloadedRejectionController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: DateGoodsUnloadedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  unloadingPermissionService: UnloadingPermissionService,
  errorHandler: ErrorHandler,
  view: DateGoodsUnloadedRejectionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    actions.requireData(arrivalId).andThen(getMandatoryPage(DateGoodsUnloadedPage)).async {
      implicit request =>
        unloadingPermissionService.getUnloadingPermission(arrivalId) flatMap {
          case Some(unloadingPermission) =>
            val form         = formProvider(unloadingPermission.dateOfPreparation)
            val preparedForm = form.fill(request.arg)
            Future.successful(Ok(view(request.userAnswers.mrn.toString, arrivalId, preparedForm)))
          case _ =>
            errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
        }
    }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      unloadingPermissionService.getUnloadingPermission(arrivalId) flatMap {
        case Some(unloadingPermission) =>
          formProvider(unloadingPermission.dateOfPreparation)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(request.userAnswers.mrn.toString, arrivalId, formWithErrors))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(DateGoodsUnloadedPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId))
            )
        case _ =>
          errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
      }
  }
}
