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
import forms.YesNoFormProvider
import models.Procedure.CannotUseRevisedDueToDiscrepancies
import models.{ArrivalId, Mode, Procedure, UserAnswers}
import navigation.Navigation
import pages.*
import pages.sections.OtherQuestionsSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AddTransitUnloadingPermissionDiscrepanciesYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddTransitUnloadingPermissionDiscrepanciesYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigation,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddTransitUnloadingPermissionDiscrepanciesYesNoView,
  userAnswersService: UserAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("addTransitUnloadingPermissionDiscrepanciesYesNo")

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode))),
          value => {
            val userAnswersF: Future[UserAnswers] =
              if (request.userAnswers.hasAnswerChanged(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, value) && !value) {
                Procedure(request.userAnswers) match {
                  case CannotUseRevisedDueToDiscrepancies =>
                    userAnswersService.retainAndTransform(
                      request.userAnswers,
                      NewAuthYesNoPage,
                      RevisedUnloadingProcedureConditionsYesNoPage,
                      GoodsTooLargeForContainerYesNoPage
                    )
                  case _ =>
                    userAnswersService.retainAndTransform(request.userAnswers, OtherQuestionsSection)
                }
              } else {
                Future.successful(request.userAnswers)
              }

            for {
              userAnswers    <- userAnswersF
              updatedAnswers <- Future.fromTry(userAnswers.set(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, updatedAnswers))
          }
        )
  }
}
