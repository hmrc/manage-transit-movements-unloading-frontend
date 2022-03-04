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
import derivable.DeriveNumberOfSeals
import forms.NewSealNumberFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.{ArrivalId, Index, Mode, UserAnswers}
import navigation.Navigator
import pages.NewSealNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.UnloadingPermissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class NewSealNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: NewSealNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  unloadingPermissionService: UnloadingPermissionService,
  errorHandler: ErrorHandler,
  checkArrivalStatus: CheckArrivalStatusProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get(NewSealNumberPage(index)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        val json = Json.obj(
          "form"      -> preparedForm,
          "mrn"       -> request.userAnswers.mrn,
          "arrivalId" -> arrivalId,
          "mode"      -> mode,
          "index"     -> index.display
        )

        renderer.render("newSealNumber.njk", json).map(Ok(_))
    }

  def onSubmit(arrivalId: ArrivalId, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkArrivalStatus(arrivalId) andThen getData(arrivalId) andThen requireData).async {
      implicit request =>
        val userAnswers: Future[Option[UserAnswers]] = request.userAnswers.get(DeriveNumberOfSeals) match {
          case Some(_) => Future.successful(Some(request.userAnswers))
          case None    => unloadingPermissionService.convertSeals(request.userAnswers)
        }

        userAnswers.flatMap {
          case Some(ua) =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => {
                  val json = Json.obj(
                    "form"      -> formWithErrors,
                    "mrn"       -> request.userAnswers.mrn,
                    "arrivalId" -> arrivalId,
                    "mode"      -> mode,
                    "index"     -> index.display
                  )

                  renderer.render("newSealNumber.njk", json).map(BadRequest(_))
                },
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(ua.set(NewSealNumberPage(index), value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(NewSealNumberPage(index), mode, updatedAnswers))
              )
          case _ =>
            errorHandler.onClientError(request, BAD_REQUEST, "errors.malformedSeals") //todo: get design and content to look at this
        }

    }

}
