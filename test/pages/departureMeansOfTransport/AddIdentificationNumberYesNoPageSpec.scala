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

package pages.departureMeansOfTransport

import org.scalacheck.Gen
import pages.behaviours.PageBehaviours

class AddIdentificationNumberYesNoPageSpec extends PageBehaviours {

  "AddIdentificationNumberYesNoPage" - {

    beRetrievable[Boolean](AddIdentificationNumberYesNoPage(index))

    beSettable[Boolean](AddIdentificationNumberYesNoPage(index))

    beRemovable[Boolean](AddIdentificationNumberYesNoPage(index))

    "cleanup" - {
      "must remove identification number when no selected" in {
        forAll(Gen.alphaNumStr) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(AddIdentificationNumberYesNoPage(index), true)
              .setValue(VehicleIdentificationNumberPage(index), value)

            val result = userAnswers.setValue(AddIdentificationNumberYesNoPage(index), false)

            result.get(VehicleIdentificationNumberPage(index)) must not be defined
        }
      }

      "must keep identification number when yes selected" in {
        forAll(Gen.alphaNumStr) {
          value =>
            val userAnswers = emptyUserAnswers
              .setValue(AddIdentificationNumberYesNoPage(index), true)
              .setValue(VehicleIdentificationNumberPage(index), value)

            val result = userAnswers.setValue(AddIdentificationNumberYesNoPage(index), true)

            result.get(VehicleIdentificationNumberPage(index)) mustBe defined
        }
      }
    }
  }
}
