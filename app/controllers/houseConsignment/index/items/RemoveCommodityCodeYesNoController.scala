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
import forms.ConfirmRemoveCommentsFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.ConfirmRemoveCommentsPage
import pages.houseConsignment.index.items.CommodityCodePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.RemoveCommodityCodeYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveCommodityCodeYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: ConfirmRemoveCommentsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveCommodityCodeYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId) {
      implicit request =>
        val insetText: Option[String] = request.userAnswers.get(CommodityCodePage(houseConsignmentIndex, itemIndex))
        Ok(view(form, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, mode, insetText))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): Action[AnyContent] =
    actions.getStatus(arrivalId).async {
      implicit request =>
        val insetText: Option[String] = request.userAnswers.get(CommodityCodePage(houseConsignmentIndex, itemIndex))
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, itemIndex, mode, insetText))),
            value =>
              if (value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(CommodityCodePage(houseConsignmentIndex, itemIndex)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex)) //todo update on navigation ticket
              } else {
                Future.successful(
                  Redirect(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
                ) //todo update on navigation ticket??
              }
          )
    }
}
