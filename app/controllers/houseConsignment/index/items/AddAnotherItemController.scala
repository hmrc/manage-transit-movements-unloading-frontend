/*
 * Copyright 2024 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.actions._
import forms.AddAnotherFormProvider
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode}
import pages.houseConsignment.index.items.DeclarationGoodsItemNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import services.GoodsReferenceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.houseConsignment.index.items.AddAnotherItemViewModel
import viewModels.houseConsignment.index.items.AddAnotherItemViewModel.AddAnotherItemViewModelProvider
import views.html.houseConsignment.index.items.AddAnotherItemView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherItemController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherItemView,
  viewModelProvider: AddAnotherItemViewModelProvider,
  goodsReferenceService: GoodsReferenceService
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherItemViewModel, houseConsignmentIndex: Index): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore, houseConsignmentIndex.display)

  def onPageLoad(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, arrivalId, houseConsignmentIndex, mode)
      Ok(view(form(viewModel, houseConsignmentIndex), request.userAnswers.mrn, arrivalId, viewModel))
  }

  def onSubmit(arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode): Action[AnyContent] = actions.requireData(arrivalId).async {
    implicit request =>
      val userAnswers = goodsReferenceService.removeEmptyItems(request.userAnswers, houseConsignmentIndex)
      val viewModel   = viewModelProvider(userAnswers, arrivalId, houseConsignmentIndex, mode)
      form(viewModel, houseConsignmentIndex)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, userAnswers.mrn, arrivalId, viewModel))),
          {
            case true =>
              val itemIndex                      = viewModel.nextIndex
              val nextDeclarationGoodsItemNumber = goodsReferenceService.getNextDeclarationGoodsItemNumber(userAnswers)
              for {
                updatedAnswers <- Future.fromTry {
                  userAnswers.set(DeclarationGoodsItemNumberPage(houseConsignmentIndex, itemIndex), nextDeclarationGoodsItemNumber)
                }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                controllers.houseConsignment.index.items.routes.DescriptionController
                  .onPageLoad(arrivalId, mode, NormalMode, houseConsignmentIndex, itemIndex)
              )
            case false =>
              sessionRepository.set(userAnswers).map {
                _ =>
                  mode match {
                    case NormalMode =>
                      Redirect(controllers.houseConsignment.routes.AddAnotherHouseConsignmentController.onPageLoad(arrivalId, mode))
                    case CheckMode =>
                      Redirect(controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex))
                  }
              }
          }
        )
  }

}
