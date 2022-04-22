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
import models.ArrivalId
import pages.{DateGoodsUnloadedPage, DateOfPreparationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
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
  view: DateGoodsUnloadedRejectionView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] =
    actions
      .requireData(arrivalId)
      .andThen(getMandatoryPage.getFirst(DateOfPreparationPage))
      .andThen(getMandatoryPage.getSecond(DateGoodsUnloadedPage)) {
        implicit request =>
          val form         = formProvider(request.arg._1)
          val preparedForm = form.fill(request.arg._2)
          Ok(view(request.userAnswers.mrn.toString, arrivalId, preparedForm))
      }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] =
    actions.requireData(arrivalId).andThen(getMandatoryPage(DateOfPreparationPage)).async {
      implicit request =>
        formProvider(request.arg)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(request.userAnswers.mrn.toString, arrivalId, formWithErrors))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(DateGoodsUnloadedPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId))
          )
    }
}
