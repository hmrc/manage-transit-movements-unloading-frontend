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

package controllers.houseConsignment.index.items

import controllers.actions._
import forms.YesNoFormProvider
import models.{ArrivalId, Index, Mode}
import pages.houseConsignment.index.items.NetWeightPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.RemoveNetWeightYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveNetWeightYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveNetWeightYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(houseConsignmentIndex: Index, itemIndex: Index): Form[Boolean] =
    formProvider("houseConsignment.index.item.removeNetWeightYesNo", houseConsignmentIndex.display, itemIndex.display)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Action[AnyContent] =
    actions.getStatus(arrivalId) {
      implicit request =>
        val insetText: Option[String] = request.userAnswers.get(NetWeightPage(houseConsignmentIndex, itemIndex)).map(_.toString())
        Ok(view(form(houseConsignmentIndex, itemIndex), request.userAnswers.mrn, arrivalId, mode, houseConsignmentIndex, itemIndex, insetText))
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, houseConsignmentIndex: Index, itemIndex: Index): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        val insetText: Option[String] = request.userAnswers.get(NetWeightPage(houseConsignmentIndex, itemIndex)).map(_.toString())
        form(houseConsignmentIndex, itemIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, mode, houseConsignmentIndex, itemIndex, insetText))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.remove(NetWeightPage(houseConsignmentIndex, itemIndex)))
                  } else { Future.successful(request.userAnswers) }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
          )
    }
}
