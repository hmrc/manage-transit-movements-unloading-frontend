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

package models

import base.SpecBase
import pages.*

class OtherThingsToReportSpec extends SpecBase {

  "OtherThingsToReport" - {

    "must return correct prefix" - {
      "when not a revised procedure" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)

        val result = OtherThingsToReport.apply(userAnswers)

        result.prefix.mustBe("otherThingsToReport.oldAuth")
      }

      "when cannot use revised procedure" - {
        "when RevisedUnloadingProcedureConditionsYesNoPage is false" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, false)

          val result = OtherThingsToReport.apply(userAnswers)

          result.prefix.mustBe("otherThingsToReport.oldAuth")
        }

        "when AddTransitUnloadingPermissionDiscrepanciesYesNoPage is true" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, true)
            .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

          val result = OtherThingsToReport.apply(userAnswers)

          result.prefix.mustBe("otherThingsToReport.oldAuth")
        }
      }

      "when a revised procedure" - {
        "and external seal replaced by customs authority" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, false)
            .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)

          val result = OtherThingsToReport.apply(userAnswers)

          result.prefix.mustBe("otherThingsToReport.newAuthAndSealsReplaced")
        }

        "and external seal not replaced by customs authority" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, false)
            .setValue(SealsReplacedByCustomsAuthorityYesNoPage, false)

          val result = OtherThingsToReport.apply(userAnswers)

          result.prefix.mustBe("otherThingsToReport.newAuth")
        }
      }
    }

    "must throw exception" - {
      "when a revised procedure" - {
        "and SealsReplacedByCustomsAuthorityYesNoPage is unpopulated" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, false)

          a[Exception].mustBe(thrownBy(OtherThingsToReport.apply(userAnswers)))
        }
      }
    }
  }
}
