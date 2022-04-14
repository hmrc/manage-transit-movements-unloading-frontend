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
import forms.ChangesToReportFormProvider
import models.messages.RemarksNonConform._
import models.{ArrivalId, Mode}
import navigation.Navigator
import pages.ChangesToReportPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ChangesToReportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangesToReportController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: ChangesToReportFormProvider,
  val controllerComponents: MessagesControllerComponents,
  checkArrivalStatus: CheckArrivalStatusProvider,
  view: ChangesToReportView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData) {
      implicit request =>
        val preparedForm = request.userAnswers.get(ChangesToReportPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, unloadingRemarkLength, mode))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, unloadingRemarkLength, mode))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ChangesToReportPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ChangesToReportPage, mode, updatedAnswers))
          )
    }
}
