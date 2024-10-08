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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "headerSection" - {

    "must render rows" in {

      val unloadedDate = arbitrary[LocalDate].sample.value
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, false)
        .setValue(DateGoodsUnloadedPage, unloadedDate)
        .setValue(CanSealsBeReadPage, true)
        .setValue(AreAnySealsBrokenPage, true)

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new CheckYourAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.sections.length mustBe 2
      result.sections.head.rows.size mustBe 4
    }

    "must render LargeUnsealedGoodsRecordDiscrepanciesYesNoPage row when false" in {
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, false)
        .setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, false)

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new CheckYourAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.sections.length mustBe 2
      result.sections.head.rows.size mustBe 2
    }

  }

  "comments section" - {

    "must render 1 rows if add comments false" in {

      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, false)
        .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new CheckYourAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.sections.length mustBe 2
      result.sections(1).rows.size mustBe 1

      result.sections(1).sectionTitle.value mustBe "Transit movement and unloading permission discrepancies"

    }

    "must render 2 rows if add comments true" in {

      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, false)
        .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
        .setValue(UnloadingCommentsPage, "Test")

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new CheckYourAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.sections.length mustBe 2
      result.sections(1).rows.size mustBe 2

      result.sections(1).sectionTitle.value mustBe "Transit movement and unloading permission discrepancies"

    }

  }

  "showDiscrepanciesLink boolean for" - {
    "old auth must be" - {
      "false when seals are present & not damaged and AddUnloadingCommentsYesNo page is false" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(CanSealsBeReadPage, true)
          .setValue(AreAnySealsBrokenPage, false)
          .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.showDiscrepanciesLink mustBe false
      }

      "true when seals are present & not damaged and AddUnloadingCommentsYesNo page is true" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(CanSealsBeReadPage, true)
          .setValue(AreAnySealsBrokenPage, false)
          .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.showDiscrepanciesLink mustBe true
      }

      "true when seals are present but can't be read" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(CanSealsBeReadPage, false)
          .setValue(AreAnySealsBrokenPage, false)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.showDiscrepanciesLink mustBe true
      }

      "true when seals are present but are broken" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(CanSealsBeReadPage, true)
          .setValue(AreAnySealsBrokenPage, true)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.showDiscrepanciesLink mustBe true
      }

      "true when seals are present but can't be read and are broken" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(CanSealsBeReadPage, false)
          .setValue(AreAnySealsBrokenPage, true)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.showDiscrepanciesLink mustBe true
      }

      "false when seals are not present and AddUnloadingCommentsYesNo page is false" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(CanSealsBeReadPage, None)
          .setValue(AreAnySealsBrokenPage, None)
          .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.showDiscrepanciesLink mustBe false
      }

      "true when seals are not present and AddUnloadingCommentsYesNo page is true" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(CanSealsBeReadPage, None)
          .setValue(AreAnySealsBrokenPage, None)
          .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new CheckYourAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        result.showDiscrepanciesLink mustBe true
      }
    }

    "new auth must be false when NewAuthPageYesNo is true" in {
      val replaced = arbitrary[Boolean].sample.value
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, true)
        .setValue(SealsReplacedByCustomsAuthorityYesNoPage, replaced)

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new CheckYourAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.showDiscrepanciesLink mustBe false
    }
  }

}
