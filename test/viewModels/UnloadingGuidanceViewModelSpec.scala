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
import pages.*
import viewModels.UnloadingGuidanceViewModel.UnloadingGuidanceViewModelProvider

class UnloadingGuidanceViewModelSpec extends SpecBase with AppWithDefaultMockFixtures {

  "UnloadingFindingsViewModel" - {
    val viewModel = new UnloadingGuidanceViewModelProvider

    "return correct text for not newAuth title" in {
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, false)
      viewModel.apply(userAnswers).title mustEqual s"unloadingGuidance.notNewAuth.title"
    }

    "return correct text for not newAuth heading" in {
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, false)
      viewModel.apply(userAnswers).heading mustEqual s"unloadingGuidance.notNewAuth.heading"
    }

    "return correct text for newAuth and goodsTooLarge = false" in {
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, true)
        .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
        .setValue(GoodsTooLargeForContainerYesNoPage, false)
        .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
      viewModel.apply(userAnswers).title mustEqual s"unloadingGuidance.newAuth.goodsTooLargeNo.title"
    }

    "return correct text for newAuth and goodsTooLarge = true" in {
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, true)
        .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
        .setValue(GoodsTooLargeForContainerYesNoPage, true)
        .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)
        .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
      viewModel.apply(userAnswers).title mustEqual s"unloadingGuidance.newAuth.goodsTooLargeYes.title"
    }

    "return correct preLinkText for newAuth and goodsTooLarge = false" in {
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, true)
        .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
        .setValue(GoodsTooLargeForContainerYesNoPage, false)
        .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
      viewModel.apply(userAnswers).preLinkText.value mustEqual "unloadingGuidance.preLinkText"
    }

    "return correct postLinkText for newAuth and goodsTooLarge = false" in {
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, true)
        .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
        .setValue(GoodsTooLargeForContainerYesNoPage, false)
        .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
      viewModel.apply(userAnswers).postLinkText.value mustEqual "unloadingGuidance.postLinkText"
    }

    "return correct para2 for not newAuth" in {
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, false)
      viewModel.apply(userAnswers).para2.value mustEqual "unloadingGuidance.para2.notNewAuth"
    }

    "return None when newAuth and goodsTooLarge = false" in {
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, true)
        .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
        .setValue(GoodsTooLargeForContainerYesNoPage, false)
        .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
      viewModel.apply(userAnswers).para2 must not be defined
    }

    "return correct para2 for newAuth and goodsTooLarge = true" in {
      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, true)
        .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
        .setValue(GoodsTooLargeForContainerYesNoPage, true)
        .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)
        .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
      viewModel.apply(userAnswers).para2.value mustEqual "unloadingGuidance.para2.newAuth.goodsTooLargeYes"
    }

  }

}
