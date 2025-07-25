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

class ProcedureSpec extends SpecBase {

  "Procedure" - {

    "when not a revised procedure" - {
      "must be UsingUnrevised" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)

        val result = Procedure.apply(userAnswers)

        result.mustEqual(Procedure.Unrevised)
      }
    }

    "when switching from revised to legacy procedure" - {
      "when RevisedUnloadingProcedureConditionsYesNoPage is false" - {
        "must be CannotUseRevised" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, false)

          val result = Procedure.apply(userAnswers)

          result.mustEqual(Procedure.CannotUseRevisedDueToConditions)
        }
      }

      "when RevisedUnloadingProcedureConditionsYesNoPage is true" - {
        "and AddTransitUnloadingPermissionDiscrepanciesYesNoPage is true" - {
          "must be CannotUseRevised" in {
            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, true)
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
              .setValue(GoodsTooLargeForContainerYesNoPage, true)
              .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

            val result = Procedure.apply(userAnswers)

            result.mustEqual(Procedure.CannotUseRevisedDueToDiscrepancies)
          }
        }
      }
    }

    "when a revised procedure" - {
      "and goods too large" - {
        "must be RevisedAndGoodsTooLarge" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, true)
            .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

          val result = Procedure.apply(userAnswers)

          result.mustEqual(Procedure.RevisedAndGoodsTooLarge)
        }
      }

      "and goods not too large" - {
        "must be RevisedAndGoodsNotTooLarge" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, false)

          val result = Procedure.apply(userAnswers)

          result.mustEqual(Procedure.RevisedAndGoodsNotTooLarge)
        }
      }

      "and AddTransitUnloadingPermissionDiscrepanciesYesNoPage is unpopulated" - {
        "must be RevisedAndGoodsTooLarge" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, true)

          val result = Procedure.apply(userAnswers)

          result.mustEqual(Procedure.RevisedAndGoodsTooLarge)
        }
      }
    }
  }
}
