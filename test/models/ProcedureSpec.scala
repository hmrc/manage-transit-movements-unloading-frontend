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
      "must be Unrevised" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)

        val result = Procedure.apply(userAnswers)

        result.mustBe(Procedure.Unrevised)
      }
    }

    "when switching from revised to legacy procedure" - {
      "when RevisedUnloadingProcedureConditionsYesNoPage is false" - {
        "must be unrevised" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, false)

          val result = Procedure.apply(userAnswers)

          result.mustBe(Procedure.Unrevised)
        }
      }

      "when RevisedUnloadingProcedureConditionsYesNoPage is true" - {
        "and LargeUnsealedGoodsRecordDiscrepanciesYesNoPage is true" - {
          "must be unrevised" in {
            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, true)
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
              .setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, true)

            val result = Procedure.apply(userAnswers)

            result.mustBe(Procedure.Unrevised)
          }
        }
      }
    }

    "when a revised procedure" - {
      "and external seal replaced by customs authority" - {
        "must be RevisedAndSealReplaced" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)

          val result = Procedure.apply(userAnswers)

          result.mustBe(Procedure.RevisedAndSealReplaced)
        }
      }

      "and external seal not replaced by customs authority" - {
        "must be RevisedAndSealNotReplaced" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(SealsReplacedByCustomsAuthorityYesNoPage, false)

          val result = Procedure.apply(userAnswers)

          result.mustBe(Procedure.RevisedAndSealNotReplaced)
        }
      }

      "and SealsReplacedByCustomsAuthorityYesNoPage is unpopulated" - {
        "must throw exception" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)

          a[Exception].mustBe(thrownBy(Procedure.apply(userAnswers)))
        }
      }
    }

    "when NewAuthYesNoPage unpopulated" - {
      "must throw exception" in {
        val userAnswers = emptyUserAnswers

        a[Exception].mustBe(thrownBy(Procedure.apply(userAnswers)))
      }
    }
  }
}
