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

package controllers.houseConsignment.index.items

import controllers.actions._
import forms.NetWeightFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator
import pages.houseConsignment.index.items.NetWeightPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.NetWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NetWeightController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: NetWeightFormProvider,
  val controllerComponents: MessagesControllerComponents,
  navigator: HouseConsignmentItemNavigator,
  view: NetWeightView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(houseConsignmentIndex: Index, itemIndex: Index) = formProvider(houseConsignmentIndex, itemIndex)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId) {
      implicit request =>
        val preparedForm = request.userAnswers.get(NetWeightPage(houseConsignmentIndex, itemIndex)) match {
          case None        => form(houseConsignmentIndex, itemIndex)
          case Some(value) => form(houseConsignmentIndex, itemIndex).fill(value.toString)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, mode))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        form(houseConsignmentIndex, itemIndex)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, mode))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(NetWeightPage(houseConsignmentIndex, itemIndex), value.toDouble))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(NetWeightPage(houseConsignmentIndex, itemIndex), mode, request.userAnswers))
          )
    }
}
