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

class NewAuthYesNoPageSpec extends PageBehaviours {

  "NewAuthYesNoPage" - {

    beRetrievable[Boolean](NewAuthYesNoPage)

    beSettable[Boolean](NewAuthYesNoPage)

    beRemovable[Boolean](NewAuthYesNoPage)

    "cleanup" - {
      "must cleanup when no selected" in {
        forAll(arbitrary[Boolean], arbitrary[Boolean], arbitrary[Boolean], nonEmptyString) {
          (goodsTooLarge, anyDiscrepancies, sealsReplaced, otherThingsToReport) =>
            val userAnswers = emptyUserAnswers
              .setValue(GoodsTooLargeForContainerYesNoPage, goodsTooLarge)
              .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, anyDiscrepancies)
              .setValue(SealsReplacedByCustomsAuthorityYesNoPage, sealsReplaced)
              .setValue(OtherThingsToReportPage, otherThingsToReport)

            val result = userAnswers.setValue(NewAuthYesNoPage, false)

            result.get(GoodsTooLargeForContainerYesNoPage) must not be defined
            result.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) must not be defined
            result.get(SealsReplacedByCustomsAuthorityYesNoPage) must not be defined
            result.get(OtherThingsToReportPage) must not be defined
        }
      }

      "must not cleanup when yes selected" in {
        forAll(arbitrary[Boolean], arbitrary[Boolean], arbitrary[Boolean], nonEmptyString, arbitrary[Boolean]) {
          (goodsTooLarge, anyDiscrepancies, sealsReplaced, otherThingsToReport, revisedUnloadingProcedureConditions) =>
            val userAnswers = emptyUserAnswers
              .setValue(GoodsTooLargeForContainerYesNoPage, goodsTooLarge)
              .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, anyDiscrepancies)
              .setValue(SealsReplacedByCustomsAuthorityYesNoPage, sealsReplaced)
              .setValue(OtherThingsToReportPage, otherThingsToReport)
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)

            val result = userAnswers.setValue(NewAuthYesNoPage, true)

            result.get(GoodsTooLargeForContainerYesNoPage) mustBe defined
            result.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) mustBe defined
            result.get(SealsReplacedByCustomsAuthorityYesNoPage) mustBe defined
            result.get(OtherThingsToReportPage) mustBe defined
            result.get(RevisedUnloadingProcedureConditionsYesNoPage) mustBe defined
        }
      }
    }
  }
}
