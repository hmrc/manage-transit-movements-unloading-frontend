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

package controllers.countriesOfRouting

import controllers.actions.*
import forms.YesNoFormProvider
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.countriesOfRouting.CountryOfRoutingPage
import pages.sections.CountryOfRoutingSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.countriesOfRouting.RemoveCountryYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveCountryYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveCountryYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def form(index: Index): Form[Boolean] =
    formProvider("countriesOfRouting.removeCountryYesNo", index)

  private def addAnother(arrivalId: ArrivalId, mode: Mode): Call =
    routes.AddAnotherCountryController.onPageLoad(arrivalId, mode)

  private def formatInsetText(userAnswers: UserAnswers, index: Index): Option[String] =
    userAnswers.get(CountryOfRoutingPage(index)).map(_.toString)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, index: Index): Action[AnyContent] = actions
    .requirePhase6AndIndex(arrivalId, CountryOfRoutingSection(index), addAnother(arrivalId, mode)) {
      implicit request =>
        val insetText = formatInsetText(request.userAnswers, index)
        Ok(view(form(index), request.userAnswers.mrn, arrivalId, index, mode, insetText))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, index: Index): Action[AnyContent] = actions
    .requirePhase6AndIndex(arrivalId, CountryOfRoutingSection(index), addAnother(arrivalId, mode))
    .async {
      implicit request =>
        val insetText = formatInsetText(request.userAnswers, index)
        form(index)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, index, mode, insetText))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeDataGroup(CountryOfRoutingSection(index)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode))
          )
    }
}
