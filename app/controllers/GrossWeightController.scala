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

import controllers.actions.*
import forms.Constants.{grossWeightDecimalPlaces, grossWeightIntegerLength}
import forms.WeightFormProvider
import models.{ArrivalId, Mode}
import navigation.ConsignmentNavigator
import pages.GrossWeightPage
import play.api.data.Form
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
  navigator: ConsignmentNavigator,
  actions: Actions,
  formProvider: WeightFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: GrossWeightView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form: Form[BigDecimal] =
    formProvider(
      prefix = "grossWeight",
      decimalPlaceCount = grossWeightDecimalPlaces,
      characterCount = grossWeightIntegerLength,
      isZeroAllowed = true
    )

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val preparedForm = request.userAnswers.get(GrossWeightPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(GrossWeightPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(GrossWeightPage, mode, updatedAnswers))
          )
    }
}
