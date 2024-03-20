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

package controllers.transportEquipment.index

import config.FrontendAppConfig
import controllers.actions._
import forms.AddAnotherFormProvider
import models.{ArrivalId, Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.GoodsReferenceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transportEquipment.index.ApplyAnotherItemViewModel
import viewModels.transportEquipment.index.ApplyAnotherItemViewModel.ApplyAnotherItemViewModelProvider
import views.html.transport.equipment.ApplyAnotherItemView

import javax.inject.Inject

class ApplyAnotherItemController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ApplyAnotherItemView,
  viewModelProvider: ApplyAnotherItemViewModelProvider,
  goodsReferenceService: GoodsReferenceService
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: ApplyAnotherItemViewModel, equipmentIndex: Index): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore, equipmentIndex.display)

  def onPageLoad(arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val availableGoodsReferences = goodsReferenceService.getGoodsReferences(request.userAnswers, equipmentIndex, None)
        val viewModel                = viewModelProvider(request.userAnswers, arrivalId, mode, equipmentIndex, availableGoodsReferences)
        viewModel.count match {
          case 0 =>
            Redirect(routes.ApplyAnItemYesNoController.onPageLoad(arrivalId, equipmentIndex, mode))
          case _ =>
            Ok(view(form(viewModel, equipmentIndex), request.userAnswers.mrn, arrivalId, viewModel))
        }
    }

  def onSubmit(arrivalId: ArrivalId, mode: Mode, equipmentIndex: Index): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val availableGoodsReferences = goodsReferenceService.getGoodsReferences(request.userAnswers, equipmentIndex, None)
        val viewModel                = viewModelProvider(request.userAnswers, arrivalId, mode, equipmentIndex, availableGoodsReferences)
        form(viewModel, equipmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors => BadRequest(view(formWithErrors, request.userAnswers.mrn, arrivalId, viewModel)),
            {
              case true =>
                Redirect(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, equipmentIndex, viewModel.nextIndex, mode))
              case false =>
                Redirect(controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(arrivalId, mode))
            }
          )
    }

}
