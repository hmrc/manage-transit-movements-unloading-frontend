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
import models.reference.GoodsReference
import models.{CheckMode, Index, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportEquipment.index.ItemPage
import viewModels.ListItem
import viewModels.transportEquipment.index.ApplyAnotherItemViewModel.ApplyAnotherItemViewModelProvider

class ApplyAnotherItemViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val availableGoodsReferences = Seq(
    GoodsReference(BigInt(1), "")
  )

  "must get list items in check mode" - {
    val mode = CheckMode

    "when there is one item" in {
      forAll(arbitrary[BigInt]) {
        item =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, itemIndex), item)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, arrivalId, mode, equipmentIndex, availableGoodsReferences)

          result.listItems.length mustBe 1
          result.title mustBe "You have applied 1 item to transport equipment 1"
          result.heading mustBe "You have applied 1 item to transport equipment 1"
          result.legend mustBe "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustBe "You cannot apply any more items. To apply another, you need to remove one first."

          result.listItems mustBe Seq(
            ListItem(
              name = s"Item ${item.toString}",
              changeUrl = Some(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, Index(0), itemIndex, mode).url),
              removeUrl = None
            )
          )
      }
    }

    "when there are multiple items" in {

      forAll(arbitrary[BigInt], arbitrary[BigInt]) {
        (item1, item2) =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, itemIndex), item1)
            .setValue(ItemPage(equipmentIndex, Index(1)), item2)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, arrivalId, mode, equipmentIndex, availableGoodsReferences)

          result.listItems.length mustBe 2
          result.title mustBe "You have applied 2 items to transport equipment 1"
          result.heading mustBe "You have applied 2 items to transport equipment 1"
          result.legend mustBe "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustBe "You cannot apply any more items. To apply another, you need to remove one first."

          result.listItems mustBe Seq(
            ListItem(
              name = s"Item ${item1.toString}",
              changeUrl = Some(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, Index(0), itemIndex, mode).url),
              removeUrl = None
            ),
            ListItem(
              name = s"Item ${item2.toString}",
              changeUrl = Some(controllers.transportEquipment.index.routes.GoodsReferenceController.onPageLoad(arrivalId, Index(0), Index(1), mode).url),
              removeUrl = None
            )
          )
      }
    }
  }

  "must get list items in normal mode" - {
    val mode = NormalMode

    "when there is one item" in {
      forAll(arbitrary[BigInt]) {
        item =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, itemIndex), item)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, arrivalId, mode, equipmentIndex, availableGoodsReferences)

          result.listItems.length mustBe 1
          result.title mustBe "You have applied 1 item to transport equipment 1"
          result.heading mustBe "You have applied 1 item to transport equipment 1"
          result.legend mustBe "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustBe "You cannot apply any more items. To apply another, you need to remove one first."

          result.listItems mustBe Seq(
            ListItem(
              name = s"Item ${item.toString}",
              changeUrl = None,
              removeUrl =
                Some(controllers.transportEquipment.index.routes.RemoveGoodsReferenceYesNoController.onPageLoad(arrivalId, equipmentIndex, itemIndex).url)
            )
          )
      }
    }

    "when there are multiple items" in {

      forAll(arbitrary[BigInt], arbitrary[BigInt]) {
        (item1, item2) =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, itemIndex), item1)
            .setValue(ItemPage(equipmentIndex, Index(1)), item2)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, arrivalId, mode, equipmentIndex, availableGoodsReferences)

          result.listItems.length mustBe 2
          result.title mustBe "You have applied 2 items to transport equipment 1"
          result.heading mustBe "You have applied 2 items to transport equipment 1"
          result.legend mustBe "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustBe "You cannot apply any more items. To apply another, you need to remove one first."

          result.listItems mustBe Seq(
            ListItem(
              name = s"Item ${item1.toString}",
              changeUrl = None,
              removeUrl =
                Some(controllers.transportEquipment.index.routes.RemoveGoodsReferenceYesNoController.onPageLoad(arrivalId, equipmentIndex, itemIndex).url)
            ),
            ListItem(
              name = s"Item ${item2.toString}",
              changeUrl = None,
              removeUrl =
                Some(controllers.transportEquipment.index.routes.RemoveGoodsReferenceYesNoController.onPageLoad(arrivalId, equipmentIndex, Index(1)).url)
            )
          )
      }
    }
  }
}
