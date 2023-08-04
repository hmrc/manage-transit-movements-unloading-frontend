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

package controllers

import controllers.actions._
import forms.GrossWeightFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.GrossWeightPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.GrossWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GrossWeightController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: GrossWeightFormProvider,
  identify: IdentifierAction,
  checkArrivalStatusProvider: CheckArrivalStatusProvider,
  val controllerComponents: MessagesControllerComponents,
  view: GrossWeightView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(itemIndex: Index) = formProvider(itemIndex)

  def onPageLoad(arrivalId: ArrivalId, houseConsignment: Index, itemIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatusProvider(arrivalId) andThen actions.requireData(arrivalId)) {
      implicit request =>
        val preparedForm = request.userAnswers.get(GrossWeightPage(houseConsignment, itemIndex)) match {
          case None        => form(itemIndex)
          case Some(value) => form(itemIndex).fill(value.toString)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignment, itemIndex, mode))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignment: Index, itemIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatusProvider(arrivalId) andThen actions.requireData(arrivalId))
      .async {
        implicit request =>
          form(itemIndex)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignment, itemIndex, mode))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(GrossWeightPage(houseConsignment, itemIndex), value.toDouble))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
            )
      }
}
