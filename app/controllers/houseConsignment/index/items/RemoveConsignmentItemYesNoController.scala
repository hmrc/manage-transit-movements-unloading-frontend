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
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.houseConsignment.index.items.ItemDescriptionPage
import pages.sections.ItemSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.items.RemoveConsignmentItemYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveConsignmentItemYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveConsignmentItemYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def formatInsetText(userAnswers: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index): Option[String] =
    userAnswers.get(ItemDescriptionPage(houseConsignmentIndex, itemIndex))

  private def form(houseConsignmentIndex: Index, itemIndex: Index): Form[Boolean] =
    formProvider("houseConsignment.removeConsignmentItemYesNo", itemIndex.display, houseConsignmentIndex.display)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        Ok(
          view(
            form(houseConsignmentIndex, itemIndex),
            request.userAnswers.mrn,
            arrivalId,
            houseConsignmentIndex,
            itemIndex,
            mode,
            formatInsetText(request.userAnswers, houseConsignmentIndex, itemIndex)
          )
        )
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        form(houseConsignmentIndex, itemIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    request.userAnswers.mrn,
                    arrivalId,
                    houseConsignmentIndex,
                    itemIndex,
                    mode,
                    formatInsetText(request.userAnswers, houseConsignmentIndex, itemIndex)
                  )
                )
              ),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.removeItem(ItemSection(houseConsignmentIndex, itemIndex)))
                  } else { Future.successful(request.userAnswers) }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.houseConsignment.index.items.routes.AddAnotherItemController.onPageLoad(arrivalId, houseConsignmentIndex, mode))
          )
    }
}
