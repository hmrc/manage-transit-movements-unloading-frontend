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
import forms.YesNoFormProvider
import models.{ArrivalId, Mode, UserAnswers}
import navigation.Navigation
import pages.AddUnloadingCommentsYesNoPage
import pages.sections.OtherQuestionsSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.transformers.IE043Transformer
import views.html.AddUnloadingCommentsYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AddUnloadingCommentsYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigation,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddUnloadingCommentsYesNoView,
  dataTransformer: IE043Transformer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("addUnloadingCommentsYesNo")

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.getStatus(arrivalId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddUnloadingCommentsYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, mode))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = actions.getStatus(arrivalId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode))),
          value => {
            def updateUserAnswers(): Future[UserAnswers] =
              if (value) {
                Future.successful(request.userAnswers)
              } else {
                for {
                  unloadingRemarks <- Future.successful(request.userAnswers.get(OtherQuestionsSection))
                  wipedAnswers = request.userAnswers.copy(data = Json.obj())
                  transformedAnswers <- dataTransformer.transform(wipedAnswers)
                  updatedAnswers     <- Future.fromTry(unloadingRemarks.fold(Try(transformedAnswers))(transformedAnswers.set(OtherQuestionsSection, _)))
                } yield updatedAnswers
              }

            for {
              userAnswers    <- updateUserAnswers()
              updatedAnswers <- Future.fromTry(userAnswers.set(AddUnloadingCommentsYesNoPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AddUnloadingCommentsYesNoPage, mode, updatedAnswers))
          }
        )
  }
}
