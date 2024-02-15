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

import controllers.actions._
import forms.SelectableFormProvider
import models.{ArrivalId, Index, Mode}
import navigation.Navigator
import pages.transportEquipment.index.GoodsReferencePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transportEquipment.index.GoodsReferenceViewModel
import views.html.transportEquipment.index.GoodsReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: SelectableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  navigator: Navigator,
  view: GoodsReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, transportEquipmentIndex: Index, goodsReferenceIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val selectedItem = request.userAnswers.get(GoodsReferencePage(transportEquipmentIndex, goodsReferenceIndex))
        val viewModel = GoodsReferenceViewModel.apply(request.userAnswers, selectedItem)
        val form      = formProvider(mode, "transport.equipment.selectItems", viewModel.items)
        val preparedForm = selectedItem match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, arrivalId, transportEquipmentIndex, goodsReferenceIndex, request.userAnswers.mrn, viewModel, mode))
    }

  def onSubmit(arrivalId: ArrivalId, transportEquipmentIndex: Index, goodsReferenceIndex: Index, mode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val selectedItem = request.userAnswers.get(GoodsReferencePage(transportEquipmentIndex, goodsReferenceIndex))
        val viewModel    = GoodsReferenceViewModel(request.userAnswers, selectedItem)

        val form = formProvider(mode, "transport.equipment.selectItems", viewModel.items)
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, arrivalId, transportEquipmentIndex, goodsReferenceIndex, request.userAnswers.mrn, viewModel, mode))
              ),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(GoodsReferencePage(transportEquipmentIndex, goodsReferenceIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(GoodsReferencePage(transportEquipmentIndex, goodsReferenceIndex), mode, updatedAnswers))
          )
    }
}
