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

import config.FrontendAppConfig
import controllers.actions._
import forms.GrossWeightAmountFormProvider
import models.{ArrivalId, Index}
import pages.GrossWeightPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.GrossWeightAmountRejectionView
import views.html.helper.form

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GrossWeightAmountRejectionController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: GrossWeightAmountFormProvider,
  val controllerComponents: MessagesControllerComponents,
  val appConfig: FrontendAppConfig,
  view: GrossWeightAmountRejectionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(index: Index) = formProvider(index)

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    actions.requireData(arrivalId).andThen(getMandatoryPage(GrossWeightPage)) {
      implicit request =>
        Ok(view(form(Index(0)).fill(request.arg), arrivalId))
    }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      form(Index(0))
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, arrivalId))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(GrossWeightPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId))
        )
  }
}
