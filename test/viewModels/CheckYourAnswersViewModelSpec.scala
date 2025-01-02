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

package viewModels

import base.SpecBase
import generators.Generators
import models.UnloadingType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.*
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "procedureSection" - {

    "must render rows" - {
      "when legacy procedure" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)
          .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)
          .setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, true)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.procedureSection.sectionTitle must not be defined
        result.procedureSection.rows.size mustBe 4
      }

      "when revised procedure" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.procedureSection.sectionTitle must not be defined
        result.procedureSection.rows.size mustBe 1
      }
    }
  }

  "sections" - {

    "must render unloading and discrepancies sections" in {

      val date     = arbitrary[LocalDate].sample.value
      val comments = nonEmptyString.sample.value
      val report   = nonEmptyString.sample.value

      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, false)
        .setValue(UnloadingTypePage, UnloadingType.Fully)
        .setValue(DateGoodsUnloadedPage, date)
        .setValue(CanSealsBeReadPage, true)
        .setValue(AreAnySealsBrokenPage, true)
        .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
        .setValue(AddCommentsYesNoPage, true)
        .setValue(UnloadingCommentsPage, comments)
        .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
        .setValue(DoYouHaveAnythingElseToReportYesNoPage, true)
        .setValue(OtherThingsToReportPage, report)

      val viewModelProvider = new CheckYourAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.sections.length mustBe 2

      result.sections.head.sectionTitle must not be defined
      result.sections.head.rows.size mustBe 4

      result.sections(1).sectionTitle.value mustBe "Transit movement and unloading permission discrepancies"
      result.sections(1).rows.size mustBe 6
    }
  }

  "showDiscrepanciesLink" - {
    "when old auth" - {
      "when seals are present & not damaged and AddUnloadingCommentsYesNo page is false" - {
        "must be false" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, false)
            .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

          val viewModelProvider = new CheckYourAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)

          result.showDiscrepanciesLink mustBe false
        }
      }

      "when seals are present & not damaged and AddUnloadingCommentsYesNo page is true" - {
        "must be true" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, false)
            .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

          val viewModelProvider = new CheckYourAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)

          result.showDiscrepanciesLink mustBe true
        }
      }

      "when seals are present but can't be read" - {
        "must be true" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, false)
            .setValue(AreAnySealsBrokenPage, false)

          val viewModelProvider = new CheckYourAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)

          result.showDiscrepanciesLink mustBe true
        }
      }

      "when seals are present but are broken" - {
        "must be true" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, true)

          val viewModelProvider = new CheckYourAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)

          result.showDiscrepanciesLink mustBe true
        }
      }

      "when seals are present but can't be read and are broken" - {
        "must be true" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, false)
            .setValue(AreAnySealsBrokenPage, true)

          val viewModelProvider = new CheckYourAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)

          result.showDiscrepanciesLink mustBe true
        }
      }

      "when seals are not present and AddUnloadingCommentsYesNo page is false" - {
        "must be false" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, None)
            .setValue(AreAnySealsBrokenPage, None)
            .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

          val viewModelProvider = new CheckYourAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)

          result.showDiscrepanciesLink mustBe false
        }
      }

      "when seals are not present and AddUnloadingCommentsYesNo page is true" - {
        "must be true" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, None)
            .setValue(AreAnySealsBrokenPage, None)
            .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

          val viewModelProvider = new CheckYourAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)

          result.showDiscrepanciesLink mustBe true
        }
      }
    }

    "new auth" - {
      "must be false" in {
        val replaced = arbitrary[Boolean].sample.value
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)
          .setValue(SealsReplacedByCustomsAuthorityYesNoPage, replaced)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.showDiscrepanciesLink mustBe false
      }
    }
  }

  "warning" - {
    "when RevisedUnloadingProcedureConditionsYesNoPage is defined and false" - {
      "must be defined" in {
        val userAnswers       = emptyUserAnswers.setValue(RevisedUnloadingProcedureConditionsYesNoPage, false)
        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        result.warning.value mustBe "Based on your answers, you cannot use the revised unloading procedure"
      }
    }

    "when RevisedUnloadingProcedureConditionsYesNoPage is defined and true" - {
      "must be undefined" in {
        val userAnswers       = emptyUserAnswers.setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        result.warning must not be defined
      }
    }

    "when LargeUnsealedGoodsRecordDiscrepanciesYesNoPage is defined and true" - {
      "must be defined" in {
        val userAnswers       = emptyUserAnswers.setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, true)
        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        result.warning.value mustBe "Based on your answers, you cannot use the revised unloading procedure"
      }
    }

    "when LargeUnsealedGoodsRecordDiscrepanciesYesNoPage is defined and false" - {
      "must be undefined" in {
        val userAnswers       = emptyUserAnswers.setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, false)
        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        result.warning must not be defined
      }
    }

    "when RevisedUnloadingProcedureConditionsYesNoPage and LargeUnsealedGoodsRecordDiscrepanciesYesNoPage are undefined" - {
      "must be undefined" in {
        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(emptyUserAnswers)
        result.warning must not be defined
      }
    }
  }

  "goodsTooLarge" - {
    "when GoodsTooLargeForContainerYesNoPage is undefined" - {
      "must be undefined" in {
        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(emptyUserAnswers)
        result.goodsTooLarge must not be defined
      }
    }

    "when GoodsTooLargeForContainerYesNoPage is defined and true" - {
      "must be defined and true" in {
        val userAnswers       = emptyUserAnswers.setValue(GoodsTooLargeForContainerYesNoPage, true)
        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        result.goodsTooLarge.value mustBe true
      }
    }

    "when GoodsTooLargeForContainerYesNoPage is defined and false" - {
      "must be defined and false" in {
        val userAnswers       = emptyUserAnswers.setValue(GoodsTooLargeForContainerYesNoPage, false)
        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        result.goodsTooLarge.value mustBe false
      }
    }
  }
}
