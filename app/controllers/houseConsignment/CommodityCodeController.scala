/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.houseConsignment

import controllers.actions._
import forms.CommodityCodeFormProvider
import models.{ArrivalId, Index, Mode}
import pages.houseConsignment.CommodityCodePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.CommodityCodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommodityCodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: CommodityCodeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CommodityCodeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId) {
      implicit request =>
        val preparedForm = request.userAnswers.get(CommodityCodePage(houseConsignmentIndex, itemIndex)) match {
          case None        => formProvider(houseConsignmentIndex, itemIndex)
          case Some(value) => formProvider(houseConsignmentIndex, itemIndex).fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, mode))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        formProvider(houseConsignmentIndex, itemIndex)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, mode))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(CommodityCodePage(houseConsignmentIndex, itemIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
          )
    }
}
