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
import forms.DescriptionFormProvider
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator
import pages.houseConsignment.index.items.ItemDescriptionPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.DescriptionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DescriptionController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  navigator: HouseConsignmentItemNavigator,
  val controllerComponents: MessagesControllerComponents,
  formProvider: DescriptionFormProvider,
  view: DescriptionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(houseConsignmentIndex: Index, itemIndex: Index): Form[String] =
    formProvider("houseConsignment.item.description", itemIndex.display, houseConsignmentIndex.display)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ItemDescriptionPage(houseConsignmentIndex, itemIndex)) match {
        case None        => form(houseConsignmentIndex, itemIndex)
        case Some(value) => form(houseConsignmentIndex, itemIndex).fill(value)
      }
      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode, houseConsignmentIndex, itemIndex))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      form(houseConsignmentIndex, itemIndex)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode, houseConsignmentIndex, itemIndex))),
          value => redirect(value, houseConsignmentIndex, itemIndex, mode)
        )
  }

  private def redirect(
    value: String,
    houseConsignmentIndex: Index,
    itemIndex: Index,
    mode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(ItemDescriptionPage(houseConsignmentIndex, itemIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(ItemDescriptionPage(houseConsignmentIndex, itemIndex), mode, request.userAnswers))
}
