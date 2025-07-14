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

package utils.answersHelpers.consignment

import generated.{ConsignmentType05, Number1}
import models.{CheckMode, Index}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.ContainerIdentificationNumberPage
import pages.transportEquipment.index.ItemPage
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import utils.answersHelpers.AnswersHelperSpecBase
import viewModels.sections.Section.AccordionSection

class TransportEquipmentAnswersHelperSpec extends AnswersHelperSpecBase {

  "TransportEquipmentAnswersHelper" - {

    "containerIdentificationNumber" - {
      val page = ContainerIdentificationNumberPage(equipmentIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new TransportEquipmentAnswersHelper(emptyUserAnswers, equipmentIndex)
          helper.containerIdentificationNumber must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new TransportEquipmentAnswersHelper(answers, equipmentIndex)
              val result = helper.containerIdentificationNumber.value

              result.key.value mustEqual "Container identification number"
              result.value.value mustEqual value
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.href mustEqual
                controllers.transportEquipment.index.routes.ContainerIdentificationNumberController.onPageLoad(arrivalId, equipmentIndex, CheckMode).url
              action.visuallyHiddenText.value mustEqual "container identification number for transport equipment 1"
              action.id mustEqual "change-container-identification-number-1"
          }
        }
      }
    }

    "containerIndicatorRow" - {
      "must return row" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data =
            basicIe043.copy(Consignment =
              Some(
                ConsignmentType05(
                  containerIndicator = Number1,
                  inlandModeOfTransport = Some("Mode")
                )
              )
            )
          )

        val helper = new TransportEquipmentAnswersHelper(userAnswers, equipmentIndex)
        val result = helper.containerIndicatorRow

        result.get.key.value mustEqual "Are you using any containers?"
        result.value.value.value mustEqual "Yes"
        result.get.actions must not be defined
      }
    }

    "transportEquipmentSeals" - {
      "must generate accordion section" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (value1, value2) =>
            val answers = emptyUserAnswers
              .setValue(SealIdentificationNumberPage(equipmentIndex, Index(0)), value1)
              .setValue(SealIdentificationNumberPage(equipmentIndex, Index(1)), value2)

            val helper = new TransportEquipmentAnswersHelper(answers, equipmentIndex)
            val result = helper.transportEquipmentSeals

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Seals"
            result.id.value mustEqual "transport-equipment-1-seals"

            val addOrRemoveLink = result.viewLinks.head
            addOrRemoveLink.id mustEqual "add-remove-transport-equipment-1-seal"
            addOrRemoveLink.text mustEqual "Add or remove seal"
            addOrRemoveLink.visuallyHidden.value mustEqual "from transport equipment 1"

            result.rows.size mustEqual 2
            result.rows.head.value.value mustEqual value1
            result.rows(1).value.value mustEqual value2
        }
      }
    }

    "transportEquipmentItems" - {
      "must generate accordion section" in {
        forAll(arbitrary[BigInt], arbitrary[BigInt]) {
          (item1, item2) =>
            val answers = emptyUserAnswers
              .setValue(ItemPage(equipmentIndex, Index(0)), item1)
              .setValue(ItemPage(equipmentIndex, Index(1)), item2)

            val helper = new TransportEquipmentAnswersHelper(answers, equipmentIndex)
            val result = helper.transportEquipmentItems

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Items applied to this transport equipment"
            result.id.value mustEqual "transport-equipment-1-items"

            val addOrRemoveLink = result.viewLinks.head
            addOrRemoveLink.id mustEqual "add-remove-transport-equipment-1-item"
            addOrRemoveLink.text mustEqual "Add or remove items from transport equipment 1"
            addOrRemoveLink.visuallyHidden must not be defined

            result.rows.size mustEqual 2
            result.rows.head.value.value mustEqual item1.toString
            result.rows(1).value.value mustEqual item2.toString
        }
      }
    }
  }
}
