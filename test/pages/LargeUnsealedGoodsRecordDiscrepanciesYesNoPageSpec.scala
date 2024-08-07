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

package pages

import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class LargeUnsealedGoodsRecordDiscrepanciesYesNoPageSpec extends PageBehaviours {

  "LargeUnsealedGoodsRecordDiscrepanciesYesNoPage" - {

    beRetrievable[Boolean](LargeUnsealedGoodsRecordDiscrepanciesYesNoPage)

    beSettable[Boolean](LargeUnsealedGoodsRecordDiscrepanciesYesNoPage)

    beRemovable[Boolean](LargeUnsealedGoodsRecordDiscrepanciesYesNoPage)

    "cleanup" - {
      "must cleanup when yes selected" in {
        forAll(arbitrary[Boolean]) {
          bool =>
            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, bool)

            val result = userAnswers.setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, true)

            result.get(NewAuthYesNoPage) must not be defined
        }
      }

      "must not cleanup when no selected" in {
        forAll(arbitrary[Boolean]) {
          bool =>
            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, bool)

            val result = userAnswers.setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, false)

            result.get(NewAuthYesNoPage) mustBe defined
        }
      }
    }
  }
}
