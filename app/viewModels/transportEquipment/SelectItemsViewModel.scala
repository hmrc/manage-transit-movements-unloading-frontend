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

package viewModels.transportEquipment

import generated.ConsignmentType05
import models.reference.Item
import models.{Index, SelectableList, UserAnswers}
import pages.sections.TransportEquipmentListSection
import pages.sections.transport.equipment.ItemsSection
import pages.transportEquipment.index.ItemPage

case class SelectItemsViewModel(items: SelectableList[Item], allItemsCount: Int)

object SelectItemsViewModel {

  def apply(userAnswers: UserAnswers, selectedItem: Option[Item] = None): SelectItemsViewModel = {
    val allItems: Seq[Item] = userAnswers.ie043Data.Consignment.map(_.allItems).toList.flatten

    val filteredList = (for {
      equipmentIndex <- 0 until userAnswers.get(TransportEquipmentListSection).map(_.value.length).getOrElse(0)
      itemIndex      <- 0 until userAnswers.get(ItemsSection(Index(equipmentIndex))).map(_.value.length).getOrElse(0)
      itemToFilter   <- userAnswers.get(ItemPage(Index(equipmentIndex), Index(itemIndex)))
    } yield itemToFilter).foldLeft(allItems) {
      (items, itemToFilter) =>
        items.filterNot(_ == itemToFilter)
    }

    SelectItemsViewModel(SelectableList(filteredList ++ selectedItem.toSeq), allItems.length)
  }

  implicit class CC043CTypeMethods(consignmentType05: ConsignmentType05) {

    def allItems: Seq[Item] = consignmentType05.HouseConsignment
      .foldLeft(Seq.empty[Item]) {
        (listOfItems, houseConsignment) =>
          houseConsignment.ConsignmentItem.map(
            item => Item(item.declarationGoodsItemNumber.toInt, item.Commodity.descriptionOfGoods)
          ) ++ listOfItems
      }
      .sortBy(_.declarationGoodsItemNumber)
  }

}
