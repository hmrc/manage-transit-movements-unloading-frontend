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
import forms.CommodityCodeFormProvider
import models.{ArrivalId, Index, Mode, RichCC043CType}
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator.HouseConsignmentItemNavigatorProvider
import pages.houseConsignment.index.items.CommodityCodePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.CommodityCodeViewModel
import viewModels.houseConsignment.index.items.CommodityCodeViewModel.CommodityCodeViewModelProvider
import views.html.houseConsignment.index.items.CommodityCodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommodityCodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: CommodityCodeFormProvider,
  navigatorProvider: HouseConsignmentItemNavigatorProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CommodityCodeView,
  viewModelProvider: CommodityCodeViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: CommodityCodeViewModel): Form[String] =
    formProvider(viewModel.requiredError)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, houseConsignmentMode: Mode, itemMode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
        val isXI      = request.userAnswers.ie043Data.hasXIOfficeOfDestination
        val preparedForm = request.userAnswers.get(CommodityCodePage(houseConsignmentIndex, itemIndex)) match {
          case None        => form(viewModel)
          case Some(value) => form(viewModel).fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, isXI, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, houseConsignmentMode: Mode, itemMode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider.apply(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
        val isXI      = request.userAnswers.ie043Data.hasXIOfficeOfDestination

        form(viewModel)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, request.userAnswers.mrn, isXI, viewModel))
              ),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(CommodityCodePage(houseConsignmentIndex, itemIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield {
                val navigator = navigatorProvider.apply(houseConsignmentMode)
                Redirect(navigator.nextPage(CommodityCodePage(houseConsignmentIndex, itemIndex), itemMode, request.userAnswers))
              }
          )
    }

}
