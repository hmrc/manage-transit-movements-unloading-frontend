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
import forms.ConfirmRemoveSealFormProvider
import javax.inject.Inject
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.{ConfirmRemoveSealPage, NewSealNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ConfirmRemoveSealView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveSealController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: ConfirmRemoveSealFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmRemoveSealView,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData) {
      implicit request =>
        request.userAnswers.get(NewSealNumberPage(index)) match {
          case Some(seal) =>
            val form = formProvider(seal)
            Ok(view(form, request.userAnswers.mrn, arrivalId, index, seal, mode))

          case _ => Redirect(routes.SessionExpiredController.onPageLoad())
        }
    }

  def onSubmit(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(NewSealNumberPage(index)) match {
          case Some(seal) =>
            formProvider(seal)
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, index, seal, mode))),
                value =>
                  if (value) {
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.remove(NewSealNumberPage(index)))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(navigator.nextPage(ConfirmRemoveSealPage, mode, updatedAnswers))
                  } else {
                    Future.successful(Redirect(navigator.nextPage(ConfirmRemoveSealPage, mode, request.userAnswers)))
                  }
              )
          case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
        }
    }
}
