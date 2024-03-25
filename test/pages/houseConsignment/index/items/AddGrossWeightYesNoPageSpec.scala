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

import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AddGrossWeightYesNoPageSpec extends PageBehaviours {

  "AddGrossWeightYesNoPageSpec" - {

    beRetrievable[Boolean](AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex))

    beSettable[Boolean](AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex))

    beRemovable[Boolean](AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex))
  }

  "cleanup" - {
    "must remove gross weight when no selected" in {
      forAll(arbitrary[BigDecimal]) {
        value =>
          val userAnswers = emptyUserAnswers
            .setValue(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), true)
            .setValue(GrossWeightPage(houseConsignmentIndex, itemIndex), value)

          val result = userAnswers.setValue(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), false)

          result.get(GrossWeightPage(houseConsignmentIndex, itemIndex)) must not be defined
      }
    }

    "must keep gross weight when yes selected" in {
      forAll(arbitrary[BigDecimal]) {
        value =>
          val userAnswers = emptyUserAnswers
            .setValue(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), true)
            .setValue(GrossWeightPage(houseConsignmentIndex, itemIndex), value)

          val result = userAnswers.setValue(AddGrossWeightYesNoPage(houseConsignmentIndex, itemIndex), true)

          result.get(GrossWeightPage(houseConsignmentIndex, itemIndex)) mustBe defined
      }
    }
  }
}
