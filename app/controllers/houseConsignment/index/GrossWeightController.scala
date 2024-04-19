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

package controllers.houseConsignment.index

import controllers.actions._
import forms.Constants.{grossWeightDecimalPlaces, grossWeightIntegerLength}
import forms.GrossWeightFormProvider
import models.requests.MandatoryDataRequest
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.houseConsignment.index.GrossWeightPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.GrossWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GrossWeightController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  navigator: Navigator,
  formProvider: GrossWeightFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: GrossWeightView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(houseConsignmentIndex: Index) =
    formProvider("houseConsignment.index.grossWeight", grossWeightDecimalPlaces, grossWeightIntegerLength, houseConsignmentIndex.display)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val preparedForm = request.userAnswers.get(GrossWeightPage(houseConsignmentIndex)) match {
          case None        => form(houseConsignmentIndex)
          case Some(value) => form(houseConsignmentIndex).fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, mode))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .async {
        implicit request =>
          form(houseConsignmentIndex)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, mode))),
              value => redirect(value, houseConsignmentIndex, mode)
            )

      }

  private def redirect(
    value: BigDecimal,
    houseConsignmentIndex: Index,
    mode: Mode
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(GrossWeightPage(houseConsignmentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(GrossWeightPage(houseConsignmentIndex), mode, request.userAnswers))
}
