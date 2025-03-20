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

import controllers.actions.*
import forms.SelectableFormProvider.GoodsReferenceTypeFormProvider
import models.{ArrivalId, Index, Mode, SelectableList}
import navigation.GoodsReferenceNavigator.GoodsReferenceNavigatorProvider
import pages.transportEquipment.index.ItemPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.GoodsReferenceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportEquipment.index.GoodsReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: GoodsReferenceNavigatorProvider,
  actions: Actions,
  formProvider: GoodsReferenceTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: GoodsReferenceView,
  goodsReferenceService: GoodsReferenceService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, transportEquipmentIndex: Index, itemIndex: Index, equipmentMode: Mode, goodsReferenceMode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId) {
      implicit request =>
        val availableGoodsReferences = goodsReferenceService.getGoodsReferences(request.userAnswers, transportEquipmentIndex, Some(itemIndex))
        val form                     = formProvider(goodsReferenceMode, "transport.equipment.selectItems", SelectableList(availableGoodsReferences))
        val preparedForm = goodsReferenceService.getGoodsReference(request.userAnswers, transportEquipmentIndex, itemIndex) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(
          view(
            preparedForm,
            arrivalId,
            transportEquipmentIndex,
            itemIndex,
            request.userAnswers.mrn,
            availableGoodsReferences,
            equipmentMode,
            goodsReferenceMode
          )
        )
    }

  def onSubmit(arrivalId: ArrivalId, transportEquipmentIndex: Index, itemIndex: Index, equipmentMode: Mode, goodsReferenceMode: Mode): Action[AnyContent] =
    actions.requireData(arrivalId).async {
      implicit request =>
        val availableGoodsReferences = goodsReferenceService.getGoodsReferences(request.userAnswers, transportEquipmentIndex, Some(itemIndex))

        val form = formProvider(goodsReferenceMode, "transport.equipment.selectItems", SelectableList(availableGoodsReferences))
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    arrivalId,
                    transportEquipmentIndex,
                    itemIndex,
                    request.userAnswers.mrn,
                    availableGoodsReferences,
                    equipmentMode,
                    goodsReferenceMode
                  )
                )
              ),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ItemPage(transportEquipmentIndex, itemIndex), value.declarationGoodsItemNumber))
                _              <- sessionRepository.set(updatedAnswers)
              } yield {
                val navigator = navigatorProvider.apply(equipmentMode)
                Redirect(navigator.nextPage(ItemPage(transportEquipmentIndex, itemIndex), goodsReferenceMode, updatedAnswers))
              }
          )
    }
}
