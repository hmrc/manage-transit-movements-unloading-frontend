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

package pages.transportEquipment.index

import models.Index
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours
import pages.sections.SealsSection
import pages.transportEquipment.index.seals.SealIdentificationNumberPage

class AddSealYesNoPageSpec extends PageBehaviours {

  "AddSealYesNoPage" - {

    beRetrievable[Boolean](AddSealYesNoPage(index))

    beSettable[Boolean](AddSealYesNoPage(index))

    beRemovable[Boolean](AddSealYesNoPage(index))

    "cleanup" - {
      "when NO selected" - {
        "must clean up SealsSection for the necessary transport equipment index" in {
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (seal1, seal2) =>
              val preChange = emptyUserAnswers
                .setValue(AddSealYesNoPage(Index(0)), true)
                .setValue(SealIdentificationNumberPage(Index(0), Index(0)), seal1)
                .setValue(SealIdentificationNumberPage(Index(0), Index(1)), seal2)
                .setValue(AddSealYesNoPage(Index(1)), true)
                .setValue(SealIdentificationNumberPage(Index(1), Index(0)), seal1)
                .setValue(SealIdentificationNumberPage(Index(1), Index(1)), seal2)
              val postChange = preChange.setValue(AddSealYesNoPage(Index(1)), false)

              postChange.get(SealsSection(Index(0))) must be(defined)
              postChange.get(SealsSection(Index(1))) mustNot be(defined)
          }
        }
      }

      "when YES selected" - {
        "must do nothing" in {
          forAll(Gen.alphaNumStr) {
            seal =>
              val preChange  = emptyUserAnswers.setValue(SealIdentificationNumberPage(Index(0), Index(0)), seal)
              val postChange = preChange.setValue(AddSealYesNoPage(Index(0)), true)

              postChange.get(SealsSection(Index(0))) must be(defined)
          }
        }
      }
    }
  }
}
