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

import controllers.actions.*
import forms.YesNoFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.HouseConsignmentNavigator
import pages.houseConsignment.index.AddItemYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.GoodsReferenceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.AddItemYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class AddItemYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: HouseConsignmentNavigator,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddItemYesNoView,
  goodsReferenceService: GoodsReferenceService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(houseConsignmentIndex: Index): Form[Boolean] =
    formProvider("houseConsignment.addItemYesNo", houseConsignmentIndex.display)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, houseConsignmentMode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val preparedForm = request.userAnswers.get(AddItemYesNoPage(houseConsignmentIndex)) match {
          case None        => form(houseConsignmentIndex)
          case Some(value) => form(houseConsignmentIndex).fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, houseConsignmentMode))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, houseConsignmentMode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        form(houseConsignmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, houseConsignmentIndex, houseConsignmentMode))
              ),
            value =>
              for {
                updatedAnswers <- Future.fromTry(
                  request.userAnswers
                    .set(AddItemYesNoPage(houseConsignmentIndex), value)
                    .flatMap {
                      userAnswers =>
                        if (value) {
                          goodsReferenceService.setNextDeclarationGoodsItemNumber(userAnswers, houseConsignmentIndex, Index(0))
                        } else {
                          Success(userAnswers)
                        }
                    }
                )
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AddItemYesNoPage(houseConsignmentIndex), houseConsignmentMode, updatedAnswers))
          )
    }
}
