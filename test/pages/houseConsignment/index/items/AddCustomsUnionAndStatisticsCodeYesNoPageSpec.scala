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

package pages.houseConsignment.index.items

import pages.behaviours.PageBehaviours

class AddCustomsUnionAndStatisticsCodeYesNoPageSpec extends PageBehaviours {

  "AddCustomsUnionAndStatisticsCodeYesNoPageSpec" - {

    beRetrievable[Boolean](AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex))

    beSettable[Boolean](AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex))

    beRemovable[Boolean](AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex))
  }

  "cleanup" - {
    "must remove CUS code when no selected" in {
      forAll(nonEmptyString) {
        value =>
          val userAnswers = emptyUserAnswers
            .setValue(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), true)
            .setValue(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), value)

          val result = userAnswers.setValue(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), false)

          result.get(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex)) must not be defined
      }
    }

    "must keep CUS code when yes selected" in {
      forAll(nonEmptyString) {
        value =>
          val userAnswers = emptyUserAnswers
            .setValue(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), true)
            .setValue(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), value)

          val result = userAnswers.setValue(AddCustomsUnionAndStatisticsCodeYesNoPage(houseConsignmentIndex, itemIndex), true)

          result.get(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex)) mustBe defined
      }
    }
  }
}
