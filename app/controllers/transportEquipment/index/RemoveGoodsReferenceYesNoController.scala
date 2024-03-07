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
import models.reference.{Item, ItemDescription}
import models.requests.SpecificDataRequestProvider1
import models.{ArrivalId, Index, RichOptionalJsArray, UserAnswers}
import pages.sections.{HouseConsignmentsSection, ItemsSection}
import pages.sections.transport.equipment.ItemSection
import pages.transportEquipment.index.ItemPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportEquipment.index.RemoveItemYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import pages.houseConsignment.index.items.ItemGoodsReferenceDescriptionPage

class RemoveGoodsReferenceYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveItemYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private type Request = SpecificDataRequestProvider1[Item]#SpecificDataRequest[_]

  private def form(equipmentIndex: Index)(implicit request: Request): Form[Boolean] =
    formProvider("transportEquipment.index.item.removeItemYesNo", equipmentIndex.display)

  private def addAnother(arrivalId: ArrivalId, equipmentIndex: Index): Call = Call("GET", "#") // TODO should go to addAnotherItem controller

  def onPageLoad(arrivalId: ArrivalId, equipmentIndex: Index, itemIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, ItemSection(equipmentIndex, itemIndex), addAnother(arrivalId, equipmentIndex))
    .andThen(getMandatoryPage.getFirst(ItemPage(equipmentIndex, itemIndex))) {
      implicit request =>
        Ok(
          view(
            form(equipmentIndex),
            request.userAnswers.mrn,
            arrivalId,
            equipmentIndex,
            itemIndex,
            insetText(equipmentIndex, itemIndex, request.userAnswers)
          )
        )
    }

  def onSubmit(arrivalId: ArrivalId, equipmentIndex: Index, itemIndex: Index): Action[AnyContent] = actions
    .requireIndex(arrivalId, ItemSection(equipmentIndex, itemIndex), addAnother(arrivalId, equipmentIndex))
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
                    insetText(equipmentIndex, itemIndex, request.userAnswers)
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
              } yield Redirect(addAnother(arrivalId, equipmentIndex))
          )
    }

  private def insetText(equipmentIndex: Index, itemIndex: Index, userAnswers: UserAnswers): Option[String] = {
    println("before ini item page...")
    val item = userAnswers.get(ItemPage(equipmentIndex, itemIndex))

    println("ini item page...")

    val description = itemDescription(userAnswers, item)

    println("description " + description)

    item.map(
      item => "Item " + item.value + " - " + description
    )
  }

  private def itemDescription(userAnswers: UserAnswers, item: Option[Item]) =
    userAnswers.allItems
      .find(
        id =>
          id.declarationGoodsItemNumber.toString == item
            .map(
              item => item.value
            )
            .getOrElse("")
      )
      .map(
        id => id.description
      )
      .getOrElse("")

  implicit class userAnswersMethods(userAnswers: UserAnswers) {

    def allItems: Seq[ItemDescription] = userAnswers
      .get(HouseConsignmentsSection)
      .mapWithIndex {
        case (_, houseIndex) =>
          userAnswers.get(ItemsSection(houseIndex)).mapWithIndex {
            case (_, itemIndex) =>
              userAnswers.get(ItemGoodsReferenceDescriptionPage(houseIndex, itemIndex))
          }
      }
      .flatten
      .flatten
  }
}
