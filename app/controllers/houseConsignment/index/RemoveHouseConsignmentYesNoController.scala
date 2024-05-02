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

package controllers.houseConsignment.index

import controllers.actions._
import forms.YesNoFormProvider
import models.{ArrivalId, Index, Mode}
import pages.sections.HouseConsignmentSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.RemoveHouseConsignmentYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveHouseConsignmentYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveHouseConsignmentYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] = actions
    .requireIndex(arrivalId, HouseConsignmentSection(houseConsignmentIndex), addAnother(arrivalId, mode)) {
      implicit request =>
        Ok(view(form(houseConsignmentIndex), request.userAnswers.mrn, arrivalId, houseConsignmentIndex, mode))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] = actions
    .requireIndex(arrivalId, HouseConsignmentSection(houseConsignmentIndex), addAnother(arrivalId, mode))
    .async {
      implicit request =>
        form(houseConsignmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(
                  BadRequest(
                    view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, mode)
                  )
                ),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeDataGroup(HouseConsignmentSection(houseConsignmentIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, mode))
          )
    }

  def form(houseConsignmentIndex: Index): Form[Boolean] =
    formProvider("houseConsignment.index.removeHouseConsignmentYesNo", houseConsignmentIndex.display)

  private def addAnother(arrivalId: ArrivalId, mode: Mode): Call =
    controllers.houseConsignment.routes.AddAnotherHouseConsignmentController.onPageLoad(arrivalId, mode)

}
