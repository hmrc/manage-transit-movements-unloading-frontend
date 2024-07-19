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
import viewModels.UnloadingGuidanceViewModel.UnloadingGuidanceViewModelProvider

class UnloadingGuidanceViewModelSpec extends SpecBase with AppWithDefaultMockFixtures {

  "UnloadingFindingsViewModel" - {
    val viewModel = new UnloadingGuidanceViewModelProvider

    "return correct text for not newAuth title" in {
      viewModel.apply(newAuth = false, Some(true)).title mustBe s"unloadingGuidance.notNewAuth.title"
    }
    "return correct text for not newAuth heading" in {
      viewModel.apply(newAuth = false, Some(true)).heading mustBe s"unloadingGuidance.notNewAuth.heading"
    }

    "return correct text for newAuth and goodsTooLarge = false" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(false)).title mustBe s"unloadingGuidance.newAuth.goodsTooLargeNo.title"
    }

    "return correct text for newAuth and goodsTooLarge = true" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(true)).title mustBe s"unloadingGuidance.newAuth.goodsTooLargeYes.title"
    }

    "return correct preLinkText for newAuth and goodsTooLarge = false" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(false)).preLinkText mustBe "unloadingGuidance.preLinkText"
    }

    "return correct postLinkText for newAuth and goodsTooLarge = false" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(false)).postLinkText mustBe "unloadingGuidance.postLinkText"
    }

    "return correct para2 for not newAuth" in {
      viewModel.apply(newAuth = false, goodsTooLarge = Some(false)).para2 mustBe Some("unloadingGuidance.para2.notNewAuth")
      viewModel.apply(newAuth = false, goodsTooLarge = Some(true)).para2 mustBe Some("unloadingGuidance.para2.notNewAuth")
    }

    "return None when newAuth and goodsTooLarge = false" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(false)).para2 mustBe None
    }

    "return correct para2 for newAuth and goodsTooLarge = true" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(true)).para2 mustBe Some("unloadingGuidance.para2.newAuth.goodsTooLargeYes")
    }

  }

}
