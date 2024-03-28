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

package viewModels.houseConsignment.index.items

import base.SpecBase
import generators.Generators
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.ItemDescriptionPage
import pages.sections.{ItemSection, ItemsSection}
import play.api.libs.json.{JsArray, Json}
import viewModels.ListItem
import viewModels.houseConsignment.index.items.AddAnotherItemViewModel.AddAnotherItemViewModelProvider

class AddAnotherItemViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AddAnotherItemViewModel" - {
    "list items" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val result = new AddAnotherItemViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, mode)
              result.listItems mustBe Nil
              result.title mustBe "You have added 0 items for house consignment 1"
              result.heading mustBe "You have added 0 items for house consignment 1"
              result.legend mustBe "Do you want to add an item for house consignment 1?"
              result.maxLimitLabel mustBe "You cannot add any more items for house consignment 1. To add another, you need to remove one first."
              result.nextIndex mustBe Index(0)
          }
        }
      }

      "must get list items" - {

        "when there is one house consignment item - with description" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(ItemDescriptionPage(houseConsignmentIndex, itemIndex), "item description")

              val result = new AddAnotherItemViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, mode)

              result.listItems.length mustBe 1
              result.listItems.head.name mustBe "Item 1 - item description"
              result.title mustBe "You have added 1 item for house consignment 1"
              result.heading mustBe "You have added 1 item for house consignment 1"
              result.legend mustBe "Do you want to add another item for house consignment 1?"
              result.maxLimitLabel mustBe "You cannot add any more items for house consignment 1. To add another, you need to remove one first."
              result.nextIndex mustBe Index(1)
          }
        }

        "when there is one house consignment item - without description" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(ItemsSection(houseConsignmentIndex), JsArray(Seq(Json.obj("foo" -> "bar"))))

              val result = new AddAnotherItemViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, mode)

              result.listItems.length mustBe 1
              result.listItems.head.name mustBe s"Item 1"
              result.title mustBe "You have added 1 item for house consignment 1"
              result.heading mustBe "You have added 1 item for house consignment 1"
              result.legend mustBe "Do you want to add another item for house consignment 1?"
              result.maxLimitLabel mustBe "You cannot add any more items for house consignment 1. To add another, you need to remove one first."
              result.nextIndex mustBe Index(1)
          }
        }

        "when there are multiple house consignment items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(ItemDescriptionPage(houseConsignmentIndex, Index(0)), "item description 1")
                .setValue(ItemDescriptionPage(houseConsignmentIndex, Index(1)), "item description 2")

              val result = new AddAnotherItemViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, mode)
              result.listItems.length mustBe 2
              result.title mustBe s"You have added 2 items for house consignment 1"
              result.heading mustBe s"You have added 2 items for house consignment 1"
              result.legend mustBe "Do you want to add another item for house consignment 1?"
              result.maxLimitLabel mustBe "You cannot add any more items for house consignment 1. To add another, you need to remove one first."
              result.nextIndex mustBe Index(2)
          }
        }

        "when one has been removed" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(ItemDescriptionPage(houseConsignmentIndex, Index(0)), "item description 1")
                .setRemoved(ItemSection(houseConsignmentIndex, Index(1)))
                .setValue(ItemDescriptionPage(houseConsignmentIndex, Index(2)), "item description 1")

              val result = new AddAnotherItemViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, mode)

              result.listItems.length mustBe 2
              result.title mustBe s"You have added 2 items for house consignment 1"
              result.heading mustBe s"You have added 2 items for house consignment 1"
              result.legend mustBe "Do you want to add another item for house consignment 1?"
              result.maxLimitLabel mustBe "You cannot add any more items for house consignment 1. To add another, you need to remove one first."
              result.nextIndex mustBe Index(3) // take 'removed item' into account when calculating the next index
          }
        }

        "and show change and remove links" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(ItemDescriptionPage(houseConsignmentIndex, Index(0)), "item description 1")
                .setValue(ItemDescriptionPage(houseConsignmentIndex, Index(1)), "item description 2")

              val result = new AddAnotherItemViewModelProvider().apply(userAnswers, arrivalId, houseConsignmentIndex, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = "Item 1 - item description 1",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.houseConsignment.index.items.routes.RemoveConsignmentItemYesNoController
                      .onPageLoad(arrivalId, houseConsignmentIndex, Index(0), mode)
                      .url
                  )
                ),
                ListItem(
                  name = "Item 2 - item description 2",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.houseConsignment.index.items.routes.RemoveConsignmentItemYesNoController
                      .onPageLoad(arrivalId, houseConsignmentIndex, Index(1), mode)
                      .url
                  )
                )
              )

              result.nextIndex mustBe Index(2)
          }
        }
      }
    }
  }
}
