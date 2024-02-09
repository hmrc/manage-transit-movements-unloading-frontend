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

import base.SpecBase
import generators.Generators
import models.reference.Item
import models.{CheckMode, Index, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportEquipment.index.ItemPage
import viewModels.ListItem
import viewModels.transportEquipment.index.ApplyAnotherItemViewModel.ApplyAnotherItemViewModelProvider

class ApplyAnotherItemViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items in check mode" - {
    val mode = CheckMode

    "when there is one item" in {
      forAll(arbitrary[Item]) {
        item =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, itemIndex), item)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, arrivalId.value, mode, equipmentIndex, isNumberItemsZero = false)

          result.listItems.length mustBe 1
          result.title mustBe "You have applied 1 item to transport equipment 1"
          result.heading mustBe "You have applied 1 item to transport equipment 1"
          result.legend mustBe "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustBe "You cannot apply any more items. To apply another, you need to remove one first."

          result.listItems mustBe Seq(
            ListItem(
              name = s"Item ${item.toString}",
              changeOrRemoveUrl = controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, Index(0), mode).url,
              prefix = "site.edit"
            )
          )
      }
    }

    "when there are multiple items" in {

      forAll(arbitrary[Item], arbitrary[Item]) {
        (item1, item2) =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, itemIndex), item1)
            .setValue(ItemPage(equipmentIndex, Index(1)), item2)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, arrivalId.value, mode, equipmentIndex, isNumberItemsZero = false)

          result.listItems.length mustBe 2
          result.title mustBe "You have applied 2 items to transport equipment 1"
          result.heading mustBe "You have applied 2 items to transport equipment 1"
          result.legend mustBe "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustBe "You cannot apply any more items. To apply another, you need to remove one first."

          result.listItems mustBe Seq(
            ListItem(
              name = s"Item ${item1.toString}",
              changeOrRemoveUrl = controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, Index(0), mode).url,
              prefix = "site.edit"
            ),
            ListItem(
              name = s"Item ${item2.toString}",
              changeOrRemoveUrl = controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, Index(0), mode).url,
              prefix = "site.edit"
            )
          )
      }
    }
  }

  "must get list items in normal mode" - {
    val mode = NormalMode

    "when there is one item" in {
      forAll(arbitrary[Item]) {
        item =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, itemIndex), item)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, arrivalId.value, mode, equipmentIndex, isNumberItemsZero = false)

          result.listItems.length mustBe 1
          result.title mustBe "You have applied 1 item to transport equipment 1"
          result.heading mustBe "You have applied 1 item to transport equipment 1"
          result.legend mustBe "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustBe "You cannot apply any more items. To apply another, you need to remove one first."

          result.listItems mustBe Seq(
            ListItem(
              name = s"Item ${item.toString}",
              changeOrRemoveUrl = "", //TODO add in remove url
              prefix = "site.delete"
            )
          )
      }
    }

    "when there are multiple items" in {

      forAll(arbitrary[Item], arbitrary[Item]) {
        (item1, item2) =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, itemIndex), item1)
            .setValue(ItemPage(equipmentIndex, Index(1)), item2)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, arrivalId.value, mode, equipmentIndex, isNumberItemsZero = false)

          result.listItems.length mustBe 2
          result.title mustBe "You have applied 2 items to transport equipment 1"
          result.heading mustBe "You have applied 2 items to transport equipment 1"
          result.legend mustBe "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustBe "You cannot apply any more items. To apply another, you need to remove one first."

          result.listItems mustBe Seq(
            ListItem(
              name = s"Item ${item1.toString}",
              changeOrRemoveUrl = "", //TODO add in remove url
              prefix = "site.delete"
            ),
            ListItem(
              name = s"Item ${item2.toString}",
              changeOrRemoveUrl = "", //TODO add in remove url
              prefix = "site.delete"
            )
          )
      }
    }
  }
}
