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
import forms.Constants.{netWeightDecimalPlaces, netWeightIntegerLength}
import forms.WeightFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator.HouseConsignmentItemNavigatorProvider
import pages.houseConsignment.index.items.NetWeightPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.NetWeightViewModel
import viewModels.houseConsignment.index.items.NetWeightViewModel.NetWeightViewModelProvider
import views.html.houseConsignment.index.items.NetWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NetWeightController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: WeightFormProvider,
  val controllerComponents: MessagesControllerComponents,
  navigatorProvider: HouseConsignmentItemNavigatorProvider,
  view: NetWeightView,
  viewModelProvider: NetWeightViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: NetWeightViewModel): Form[BigDecimal] =
    formProvider("netWeight", viewModel.requiredError, netWeightDecimalPlaces, netWeightIntegerLength)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, houseConsignmentMode: Mode, itemMode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val viewModel = viewModelProvider.apply(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
        val preparedForm = request.userAnswers.get(NetWeightPage(houseConsignmentIndex, itemIndex)) match {
          case None        => form(viewModel)
          case Some(value) => form(viewModel).fill(value)
        }

        Ok(view(preparedForm, request.userAnswers.mrn, viewModel))
    }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, houseConsignmentMode: Mode, itemMode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val viewModel = viewModelProvider.apply(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
        form(viewModel)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, request.userAnswers.mrn, viewModel))
              ),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(NetWeightPage(houseConsignmentIndex, itemIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield {
                val navigator = navigatorProvider.apply(houseConsignmentMode)
                Redirect(navigator.nextPage(NetWeightPage(houseConsignmentIndex, itemIndex), itemMode, request.userAnswers))
              }
          )
    }
}
