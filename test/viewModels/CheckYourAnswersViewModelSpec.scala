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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.UnloadingType
import org.scalacheck.Arbitrary.arbitrary
import pages.*
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  "procedureSection" - {

    "must render rows" - {
      "when legacy procedure" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.procedureSection.sectionTitle must not be defined
        result.procedureSection.rows.size mustEqual 1
      }

      "when switching from legacy to revised procedure" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)
          .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)
          .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.procedureSection.sectionTitle must not be defined
        result.procedureSection.rows.size mustEqual 4
      }

      "when revised procedure" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)
          .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
          .setValue(GoodsTooLargeForContainerYesNoPage, false)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.procedureSection.sectionTitle must not be defined
        result.procedureSection.rows.size mustEqual 3
      }
    }
  }

  "sections" - {

    "must render unloading and discrepancies sections" - {

      "when legacy procedure" in {

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
          .setValue(UnloadingCommentsPage, comments)
          .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
          .setValue(DoYouHaveAnythingElseToReportYesNoPage, true)
          .setValue(OtherThingsToReportPage, report)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.sections.length mustEqual 2

        result.sections.head.sectionTitle must not be defined
        result.sections.head.rows.size mustEqual 4

        result.sections(1).sectionTitle.value mustEqual "Transit movement and unloading permission discrepancies"
        result.sections(1).rows.size mustEqual 5
      }

      "when revised procedure" - {
        "when goods too large" in {

          val comments = nonEmptyString.sample.value
          val report   = nonEmptyString.sample.value

          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, true)
            .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, true)
            .setValue(UnloadingCommentsPage, comments)
            .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
            .setValue(DoYouHaveAnythingElseToReportYesNoPage, true)
            .setValue(OtherThingsToReportPage, report)

          val viewModelProvider = new CheckYourAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)

          result.sections.length mustEqual 2

          result.sections.head.sectionTitle must not be defined
          result.sections.head.rows.size mustEqual 2

          result.sections(1).sectionTitle.value mustEqual "Transit movement and unloading permission discrepancies"
          result.sections(1).rows.size mustEqual 4
        }
      }
    }
  }

  "showDiscrepanciesLink" - {
    "when legacy procedure" - {
      "when seals are present & not damaged and AddUnloadingCommentsYesNo page is false" - {
        "must be false" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, false)
            .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

          val viewModelProvider = new CheckYourAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)

          result.showDiscrepanciesLink mustEqual false
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

          result.showDiscrepanciesLink mustEqual true
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

          result.showDiscrepanciesLink mustEqual true
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

          result.showDiscrepanciesLink mustEqual true
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

          result.showDiscrepanciesLink mustEqual true
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

          result.showDiscrepanciesLink mustEqual false
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

          result.showDiscrepanciesLink mustEqual true
        }
      }
    }

    "revised procedure" - {
      "when not switching to legacy procedure" - {
        "must be false" in {
          val replaced = arbitrary[Boolean].sample.value
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, false)
            .setValue(SealsReplacedByCustomsAuthorityYesNoPage, replaced)

          val viewModelProvider = new CheckYourAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)

          result.showDiscrepanciesLink mustEqual false
        }
      }

      "when switching to legacy procedure" - {
        "when adding discrepancies" - {
          "must be true" in {
            val replaced = arbitrary[Boolean].sample.value
            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, true)
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
              .setValue(GoodsTooLargeForContainerYesNoPage, true)
              .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
              .setValue(SealsReplacedByCustomsAuthorityYesNoPage, replaced)

            val viewModelProvider = new CheckYourAnswersViewModelProvider()
            val result            = viewModelProvider.apply(userAnswers)

            result.showDiscrepanciesLink mustEqual true
          }
        }
      }
    }
  }
}
