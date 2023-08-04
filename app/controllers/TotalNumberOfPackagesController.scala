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
import forms.TotalNumberOfPackagesFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.TotalNumberOfPackagesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TotalNumberOfPackagesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TotalNumberOfPackagesController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: TotalNumberOfPackagesFormProvider,
  identify: IdentifierAction,
  checkArrivalStatusProvider: CheckArrivalStatusProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TotalNumberOfPackagesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatusProvider(arrivalId) andThen actions.requireData(arrivalId)) {
      implicit request =>
        val form = formProvider(index)

        val preparedForm = request.userAnswers.get(TotalNumberOfPackagesPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, arrivalId, request.userAnswers.mrn, index, mode))

    }

  def onSubmit(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatusProvider(arrivalId) andThen actions.requireData(arrivalId)).async {
      implicit request =>
        val form = formProvider(index)

        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, arrivalId, request.userAnswers.mrn, index, mode))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(TotalNumberOfPackagesPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(TotalNumberOfPackagesPage, mode, updatedAnswers))
          )
    }
}
