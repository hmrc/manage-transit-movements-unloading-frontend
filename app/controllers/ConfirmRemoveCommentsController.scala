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
import forms.ConfirmRemoveCommentsFormProvider
import javax.inject.Inject
import models.{ArrivalId, Mode}
import navigation.Navigator
import pages.{ChangesToReportPage, ConfirmRemoveCommentsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveCommentsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: ConfirmRemoveCommentsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        val json = Json.obj(
          "form"      -> form,
          "mode"      -> mode,
          "mrn"       -> request.userAnswers.mrn,
          "arrivalId" -> arrivalId,
          "radios"    -> Radios.yesNo(form("value"))
        )

        renderer.render("confirmRemoveComments.njk", json).map(Ok(_))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => {

              val json = Json.obj(
                "form"      -> formWithErrors,
                "mode"      -> mode,
                "mrn"       -> request.userAnswers.mrn,
                "arrivalId" -> arrivalId,
                "radios"    -> Radios.yesNo(formWithErrors("value"))
              )

              renderer.render("confirmRemoveComments.njk", json).map(BadRequest(_))
            },
            value =>
              if (value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(ChangesToReportPage))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(ConfirmRemoveCommentsPage, mode, updatedAnswers))
              } else {
                Future.successful(Redirect(navigator.nextPage(ConfirmRemoveCommentsPage, mode, request.userAnswers)))
              }
          )
    }
}
