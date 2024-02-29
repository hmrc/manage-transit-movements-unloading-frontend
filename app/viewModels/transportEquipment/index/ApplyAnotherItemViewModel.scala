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

package viewModels.transportEquipment.index

import config.FrontendAppConfig
import controllers.transportEquipment.index.routes
import models.reference.Item
import models.{ArrivalId, CheckMode, Index, Mode, NormalMode, UserAnswers}
import pages.sections.transport.equipment.ItemsSection
import pages.transportEquipment.index.ItemPage
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class ApplyAnotherItemViewModel(listItems: Seq[ListItem], onSubmitCall: Call, equipmentIndex: Index, isNumberItemsZero: Boolean)
    extends AddAnotherViewModel {
  override val prefix: String = "transport.equipment.applyAnotherItem"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxItems

  override def title(implicit messages: Messages): String = if (count == 1) {
    messages(s"$prefix.singular.title", equipmentIndex.display)
  } else {
    messages(s"$prefix.plural.title", count, equipmentIndex.display)
  }

  override def heading(implicit messages: Messages): String = if (count == 1) {
    messages(s"$prefix.singular.heading", equipmentIndex.display)
  } else {
    messages(s"$prefix.plural.heading", count, equipmentIndex.display)
  }

  override def legend(implicit messages: Messages): String  = messages(s"$prefix.label", equipmentIndex.display)
  def noMoreItemsLabel(implicit messages: Messages): String = messages(s"$prefix.noMoreItems.label")

  override def allowMore(implicit config: FrontendAppConfig): Boolean = count < maxCount && !isNumberItemsZero
}

object ApplyAnotherItemViewModel {

  class ApplyAnotherItemViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: String, mode: Mode, equipmentIndex: Index, isNumberItemsZero: Boolean)(implicit
      messages: Messages
    ): ApplyAnotherItemViewModel = {

      val listItems = userAnswers
        .get(ItemsSection(equipmentIndex))
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, i) =>
            val itemIndex = Index(i)

            def itemPrefix(item: String) = messages("transport.item.prefix", item)

            def buildListItem(item: Item): ListItem = mode match {
              case CheckMode =>
                ListItem(
                  name = itemPrefix(item.toString),
                  changeUrl = Some(
                    controllers.transportEquipment.index.routes.GoodsReferenceController.onSubmit(ArrivalId(arrivalId), equipmentIndex, itemIndex, mode).url
                  ),
                  removeUrl = None
                )
              case NormalMode =>
                ListItem(
                  name = itemPrefix(item.toString),
                  changeUrl = None,
                  removeUrl = Some("") //TODO Some(routes.RemoveItemController.onPageLoad(departureId, mode, equipmentIndex, itemIndex).url)
                )
            }

            userAnswers
              .get(ItemPage(equipmentIndex, itemIndex))
              .map(
                item => buildListItem(item)
              )
        }
        .toSeq

      new ApplyAnotherItemViewModel(
        listItems,
        onSubmitCall = routes.ApplyAnotherItemController.onSubmit(arrivalId, mode, equipmentIndex),
        equipmentIndex,
        isNumberItemsZero
      )
    }
  }
}
