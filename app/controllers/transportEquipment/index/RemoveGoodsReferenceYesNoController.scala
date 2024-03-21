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
import forms.YesNoFormProvider
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.sections.transport.equipment.ItemSection
import pages.transportEquipment.index.ItemPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.GoodsReferenceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportEquipment.index.RemoveItemYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveGoodsReferenceYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveItemYesNoView,
  goodsReferenceService: GoodsReferenceService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(equipmentIndex: Index): Form[Boolean] =
    formProvider("transportEquipment.index.item.removeItemYesNo", equipmentIndex.display)

  private def addAnother(arrivalId: ArrivalId, equipmentIndex: Index, equipmentMode: Mode, goodsReferenceMode: Mode): Call =
    controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(arrivalId, equipmentMode, goodsReferenceMode, equipmentIndex)

  def onPageLoad(arrivalId: ArrivalId, equipmentIndex: Index, itemIndex: Index, equipmentMode: Mode, goodsReferenceMode: Mode): Action[AnyContent] = actions
    .requireIndex(arrivalId, ItemSection(equipmentIndex, itemIndex), addAnother(arrivalId, equipmentIndex, equipmentMode, goodsReferenceMode))
    .andThen(getMandatoryPage.getFirst(ItemPage(equipmentIndex, itemIndex))) {
      implicit request =>
        Ok(
          view(
            form(equipmentIndex),
            request.userAnswers.mrn,
            arrivalId,
            equipmentIndex,
            itemIndex,
            equipmentMode,
            goodsReferenceMode,
            insetText(request.userAnswers, equipmentIndex, itemIndex)
          )
        )
    }

  def onSubmit(arrivalId: ArrivalId, equipmentIndex: Index, itemIndex: Index, equipmentMode: Mode, goodsReferenceMode: Mode): Action[AnyContent] = actions
    .requireIndex(arrivalId, ItemSection(equipmentIndex, itemIndex), addAnother(arrivalId, equipmentIndex, equipmentMode, goodsReferenceMode))
    .andThen(getMandatoryPage.getFirst(ItemPage(equipmentIndex, itemIndex)))
    .async {
      implicit request =>
        form(equipmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    request.userAnswers.mrn,
                    arrivalId,
                    equipmentIndex,
                    itemIndex,
                    equipmentMode,
                    goodsReferenceMode,
                    insetText(request.userAnswers, equipmentIndex, itemIndex)
                  )
                )
              ),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.remove(ItemSection(equipmentIndex, itemIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(arrivalId, equipmentIndex, equipmentMode, goodsReferenceMode))
          )
    }

  private def insetText(userAnswers: UserAnswers, equipmentIndex: Index, itemIndex: Index)(implicit messages: Messages): Option[String] =
    goodsReferenceService.getGoodsReference(userAnswers, equipmentIndex, itemIndex).map(_.asString)
}
