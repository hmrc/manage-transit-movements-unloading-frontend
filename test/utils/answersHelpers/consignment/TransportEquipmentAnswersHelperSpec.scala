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

import models.{CheckMode, Index}
import org.scalacheck.Gen
import pages.ContainerIdentificationNumberPage
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import utils.answersHelpers.AnswersHelperSpecBase

class TransportEquipmentAnswersHelperSpec extends AnswersHelperSpecBase {

  "TransportEquipmentAnswersHelper" - {

    "containerIdentificationNumber" - {
      val page = ContainerIdentificationNumberPage(equipmentIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new TransportEquipmentAnswersHelper(emptyUserAnswers, equipmentIndex)
          helper.containerIdentificationNumber mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new TransportEquipmentAnswersHelper(answers, equipmentIndex)
              val result = helper.containerIdentificationNumber.value

              result.key.value mustBe "Container identification number"
              result.value.value mustBe value
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.href mustBe
                controllers.transportEquipment.index.routes.ContainerIdentificationNumberController.onPageLoad(arrivalId, equipmentIndex, CheckMode).url
              action.visuallyHiddenText.value mustBe "container identification number for transport equipment 1"
              action.id mustBe "change-container-identification-number-1"
          }
        }
      }
    }

    "transportEquipmentSeals" - {
      "must generate row for each seal" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (value1, value2) =>
            val answers = emptyUserAnswers
              .setValue(SealIdentificationNumberPage(equipmentIndex, Index(0)), value1)
              .setValue(SealIdentificationNumberPage(equipmentIndex, Index(1)), value2)

            val helper = new TransportEquipmentAnswersHelper(answers, equipmentIndex)
            val result = helper.transportEquipmentSeals

            result.size mustBe 2
            result.head.value.value mustBe value1
            result(1).value.value mustBe value2
        }
      }
    }
  }
}
