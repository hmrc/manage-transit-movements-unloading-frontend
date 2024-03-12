/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.answersHelpers.consignment.transportEquipment

import models.reference.Item
import org.scalacheck.Arbitrary.arbitrary
import pages.transportEquipment.index.ItemPage
import utils.answersHelpers.AnswersHelperSpecBase

class ItemAnswersHelperSpec extends AnswersHelperSpecBase {

  "ItemAnswersHelper" - {

    "transportEquipmentItem" - {
      val page = ItemPage(equipmentIndex, itemIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new ItemAnswersHelper(emptyUserAnswers, equipmentIndex, itemIndex)
          helper.transportEquipmentItem mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Item]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new ItemAnswersHelper(answers, equipmentIndex, itemIndex)
              val result = helper.transportEquipmentItem.value

              result.key.value mustBe "Item 1"
              result.value.value mustBe value.toString
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.href mustBe "#" //TODO: Add href for goods ref controller
              action.visuallyHiddenText.value mustBe "item 1 for transport equipment 1"
              action.id mustBe "change-consignment-item-details-1-1"
          }
        }
      }
    }
  }
}
