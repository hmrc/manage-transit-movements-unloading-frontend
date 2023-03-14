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
import forms.ConfirmRemoveSealFormProvider
import models.requests.SpecificDataRequestProvider1
import models.{ArrivalId, Index, Mode, Seal}
import navigation.Navigator
import pages.NewSealPage
import pages.sections.NewSealSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ConfirmRemoveSealView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveSealController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: ConfirmRemoveSealFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmRemoveSealView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, equipmentIndex: Index, sealIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).andThen(getMandatoryPage(NewSealPage(equipmentIndex, sealIndex))) {
      implicit request =>
        val form = formProvider(request.arg)
        Ok(view(form, request.userAnswers.mrn, arrivalId, equipmentIndex, sealIndex, request.arg, mode))
    }

  def onSubmit(arrivalId: ArrivalId, equipmentIndex: Index, sealIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).andThen(getMandatoryPage(NewSealPage(equipmentIndex, sealIndex))).async {
      implicit request =>
        formProvider(request.arg)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, equipmentIndex, sealIndex, request.arg, mode))),
            value =>
              if (value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(NewSealSection(equipmentIndex, sealIndex)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId))
              } else {
                Future.successful(Redirect(controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId)))
              }
          )
    }

}
