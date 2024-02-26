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

import pages.ContainerIdentificationNumberPage
import pages.behaviours.PageBehaviours

class AddContainerIdentificationNumberYesNoPageSpec extends PageBehaviours {

  "AddContainerIdentificationNumberPage" - {

    beRetrievable[Boolean](AddContainerIdentificationNumberYesNoPage(index))

    beSettable[Boolean](AddContainerIdentificationNumberYesNoPage(index))

    beRemovable[Boolean](AddContainerIdentificationNumberYesNoPage(index))

    "cleanup" - {
      "must remove container identification number page when no selected" in {
        forAll(nonEmptyString) {
          containerIdentificationNumber =>
            val userAnswers = emptyUserAnswers
              .setValue(AddContainerIdentificationNumberYesNoPage(index), true)
              .setValue(ContainerIdentificationNumberPage(index), containerIdentificationNumber)

            val result = userAnswers.setValue(AddContainerIdentificationNumberYesNoPage(index), false)

            result.get(ContainerIdentificationNumberPage(index)) must not be defined
        }
      }

      "must keep container identification number when yes selected" in {
        forAll(nonEmptyString) {
          containerIdentificationNumber =>
            val userAnswers = emptyUserAnswers
              .setValue(AddContainerIdentificationNumberYesNoPage(index), true)
              .setValue(ContainerIdentificationNumberPage(index), containerIdentificationNumber)

            val result = userAnswers.setValue(AddContainerIdentificationNumberYesNoPage(index), true)

            result.get(ContainerIdentificationNumberPage(index)) mustBe defined
        }
      }
    }
  }
}
