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

package viewModels.transportEquipment

import base.SpecBase
import generators.Generators
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.ContainerIdentificationNumberPage
import pages.sections.TransportEquipmentSection
import pages.transportEquipment.index.AddContainerIdentificationNumberYesNoPage
import viewModels.ListItem
import viewModels.transportEquipment.AddAnotherEquipmentViewModel.AddAnotherEquipmentViewModelProvider

class AddAnotherEquipmentViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AddAnotherEquipmentViewModelSpec" - {
    "list items" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems mustBe Nil
              result.title mustBe "You have added 0 transport equipment"
              result.heading mustBe "You have added 0 transport equipment"
              result.legend mustBe "Do you want to add any transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."
              result.nextIndex mustBe Index(0)
          }
        }
      }

      "must get list items" - {

        "when there is one transport equipment - with container id" in {
          forAll(arbitrary[Mode], Gen.alphaNumStr) {
            (mode, containerId) =>
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), true)

              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems.length mustBe 1
              result.listItems.head.name mustBe s"Transport Equipment 1 - Container $containerId"
              result.title mustBe "You have added 1 transport equipment"
              result.heading mustBe "You have added 1 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."
              result.nextIndex mustBe Index(1)
          }
        }

        "when there is one transport equipment - without container id" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), false)

              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems.length mustBe 1
              result.listItems.head.name mustBe s"Transport Equipment 1"
              result.title mustBe "You have added 1 transport equipment"
              result.heading mustBe "You have added 1 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."
              result.nextIndex mustBe Index(1)
          }
        }

        "when there are multiple transport equipment" in {
          forAll(arbitrary[Mode], Gen.alphaNumStr) {
            (mode, containerId) =>
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), true)
                .setValue(ContainerIdentificationNumberPage(Index(1)), containerId)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(1)), true)

              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems.length mustBe 2
              result.title mustBe s"You have added 2 transport equipment"
              result.heading mustBe s"You have added 2 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."
              result.nextIndex mustBe Index(2)
          }
        }

        "when one has been removed" in {
          forAll(arbitrary[Mode], Gen.alphaNumStr) {
            (mode, containerId) =>
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), true)
                .setRemoved(TransportEquipmentSection(Index(1)))
                .setValue(ContainerIdentificationNumberPage(Index(2)), containerId)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(2)), true)

              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, arrivalId, mode)
              result.listItems.length mustBe 2
              result.title mustBe s"You have added 2 transport equipment"
              result.heading mustBe s"You have added 2 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."
              result.nextIndex mustBe Index(3) // take 'removed item' into account when calculating the next index
          }
        }

        "and show change and remove links" in {
          forAll(arbitrary[Mode], Gen.alphaNumStr) {
            (mode, containerId) =>
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), true)
                .setValue(ContainerIdentificationNumberPage(Index(1)), containerId)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(1)), true)

              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, arrivalId, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = s"Transport Equipment 1 - Container $containerId",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.transportEquipment.index.routes.RemoveTransportEquipmentYesNoController
                      .onPageLoad(arrivalId, mode, Index(0))
                      .url
                  )
                ),
                ListItem(
                  name = s"Transport Equipment 2 - Container $containerId",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.transportEquipment.index.routes.RemoveTransportEquipmentYesNoController
                      .onPageLoad(arrivalId, mode, Index(1))
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
