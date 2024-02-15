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

import generated.GoodsReferenceType02
import models.reference.Item
import models.{Index, SelectableList, UserAnswers}
import pages.sections.transport.equipment.{EquipmentsSection, ItemsSection}
import pages.transportEquipment.index.ItemPage

//case class GoodsReferenceViewModel(items: SelectableList[Item], allItemsCount: Int)
//
//object GoodsReferenceViewModel {
//
//  def apply(userAnswers: UserAnswers, selectedItem: Option[Item]): GoodsReferenceViewModel = {
//    val allItems: Seq[GoodsReferenceType02] = userAnswers.ie043Data.Consignment
//      .map(
//        consignment =>
//          consignment.TransportEquipment.flatMap(
//            equipment => equipment.GoodsReference
//          )
//      )
//      .toList
//      .flatten
//
//    val getItems: Seq[Item] = for {
//      equipmentIndex <- 0 until userAnswers.get(EquipmentsSection).map(_.value.length).getOrElse(0)
//      itemIndex      <- 0 until userAnswers.get(ItemsSection(Index(equipmentIndex))).map(_.value.length).getOrElse(0)
//      itemToFilter   <- userAnswers.get(ItemPage(Index(equipmentIndex), Index(itemIndex)))
//    } yield itemToFilter
//
//    val filteredList: Seq[Item] = getItems.foldLeft(
//      allItems.map(
//        goodsReferenceType0 => Item(goodsReferenceType0.declarationGoodsItemNumber.toInt, goodsReferenceType0.sequenceNumber)
//      )
//    ) {
//      (items, itemToFilter) =>
//        items.filterNot(_ == itemToFilter)
//    }
//
//    GoodsReferenceViewModel(SelectableList(filteredList ++ selectedItem.toSeq), filteredList.length)
//  }
//}
