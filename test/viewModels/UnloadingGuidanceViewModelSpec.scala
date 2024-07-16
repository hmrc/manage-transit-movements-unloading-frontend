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

class UnloadingGuidanceViewModelSpec extends SpecBase {

  "UnloadingFindingsViewModel" - {
    val viewModel  = UnloadingGuidanceViewModel
    val testString = "testString"

    "return correct text for not newAuth" in {
      viewModel.apply(newAuth = false, Some(true)).dynamicText(testString) mustBe s"unloadingGuidance.notNewAuth.$testString"
    }

    "return correct text for newAuth and goodsTooLarge = false" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(false)).dynamicText(testString) mustBe s"unloadingGuidance.newAuth.goodsTooLargeNo.$testString"
    }

    "return correct text for newAuth and goodsTooLarge = true" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(true)).dynamicText(testString) mustBe s"unloadingGuidance.newAuth.goodsTooLargeYes.$testString"
    }

    "return correct preLinkText for newAuth and goodsTooLarge = false" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(false)).preLinkText() mustBe "unloadingGuidance.preLinkText"
    }

    "return empty preLinkText for not newAuth" in {
      viewModel.apply(newAuth = false, goodsTooLarge = Some(false)).preLinkText() mustBe ""
      viewModel.apply(newAuth = false, goodsTooLarge = Some(true)).preLinkText() mustBe ""
    }

    "return empty preLinkText for newAuth and goodsTooLarge = true" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(true)).preLinkText() mustBe ""
    }

    "return correct postLinkText for newAuth and goodsTooLarge = false" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(false)).postLinkText() mustBe "unloadingGuidance.postLinkText"
    }

    "return empty postLinkText for not newAuth" in {
      viewModel.apply(newAuth = false, goodsTooLarge = Some(false)).postLinkText() mustBe ""
      viewModel.apply(newAuth = false, goodsTooLarge = Some(true)).postLinkText() mustBe ""
    }

    "return empty postLinkText for newAuth and goodsTooLarge = true" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(true)).postLinkText() mustBe ""
    }

    "return correct para2 for not newAuth" in {
      viewModel.apply(newAuth = false, goodsTooLarge = Some(false)).para2() mustBe "unloadingGuidance.para2.notNewAuth"
      viewModel.apply(newAuth = false, goodsTooLarge = Some(true)).para2() mustBe "unloadingGuidance.para2.notNewAuth"
    }

    "return empty para2 for newAuth and goodsTooLarge = false" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(false)).para2() mustBe ""
    }

    "return correct para2 for newAuth and goodsTooLarge = true" in {
      viewModel.apply(newAuth = true, goodsTooLarge = Some(true)).para2() mustBe "unloadingGuidance.para2.newAuth.goodsTooLargeYes"
    }

  }

}
