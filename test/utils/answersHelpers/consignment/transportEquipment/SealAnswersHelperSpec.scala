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

import org.scalacheck.Gen
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import utils.answersHelpers.AnswersHelperSpecBase

class SealAnswersHelperSpec extends AnswersHelperSpecBase {

  "SealAnswersHelper" - {

    "transportEquipmentSeal" - {
      val page = SealIdentificationNumberPage(equipmentIndex, sealIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new SealAnswersHelper(emptyUserAnswers, equipmentIndex, sealIndex)
          helper.transportEquipmentSeal(sealIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new SealAnswersHelper(answers, equipmentIndex, sealIndex)
              val result = helper.transportEquipmentSeal(sealIndex).value

              result.key.value mustBe "Seal 1"
              result.value.value mustBe value
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.href mustBe "#"
              action.visuallyHiddenText.value mustBe "seal identification number"
              action.id mustBe "change-seal-details-1"
          }
        }
      }
    }
  }
}
