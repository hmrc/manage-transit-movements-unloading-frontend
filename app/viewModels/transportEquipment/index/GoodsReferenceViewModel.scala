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

import models.reference.Item
import models.{SelectableList, UserAnswers}

case class GoodsReferenceViewModel(items: SelectableList[Item], allItemsCount: Int)

object GoodsReferenceViewModel {

  def apply(userAnswers: UserAnswers, selectedItem: Option[Item]): GoodsReferenceViewModel = {
    val allItems = userAnswers.ie043Data.Consignment.map(
      consignment =>
        consignment.TransportEquipment.flatMap(
          equipment => equipment.GoodsReference
        )
    )

    val transformedItems: Seq[Item] = allItems match {
      case Some(ie43Items) =>
        ie43Items.map(
          goodsReference => Item(goodsReference.declarationGoodsItemNumber.toInt, goodsReference.sequenceNumber)
        )
      case None => Seq.empty[Item]
    }

    val filteredList = (for {
      itemToFilter <- selectedItem
    } yield itemToFilter).foldLeft(transformedItems) {
      (items, itemToFilter) =>
        items.filterNot(_ == itemToFilter)
    }

    GoodsReferenceViewModel(SelectableList(filteredList), filteredList.length)
  }
}
