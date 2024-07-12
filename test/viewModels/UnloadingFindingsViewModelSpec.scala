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

class UnloadingFindingsViewModelSpec extends SpecBase {

  "UnloadingFindingsViewModel" - {
    val viewModel = UnloadingGuidanceViewModel()

    "return correct title for not newAuth" in {
      viewModel.title(newAuth = false, goodsTooLarge = false) mustBe "unloadingGuidance.notNewAuth.title"
      viewModel.title(newAuth = false, goodsTooLarge = true) mustBe "unloadingGuidance.notNewAuth.title"
    }

    "return correct title for newAuth and goodsTooLarge = false" in {
      viewModel.title(newAuth = true, goodsTooLarge = false) mustBe "unloadingGuidance.newAuth.goodsTooLargeNo.title"
    }

    "return correct title for newAuth and goodsTooLarge = true" in {
      viewModel.title(newAuth = true, goodsTooLarge = true) mustBe "unloadingGuidance.newAuth.goodsTooLargeYes.title"
    }

    "return correct heading for not newAuth" in {
      viewModel.heading(newAuth = false, goodsTooLarge = false) mustBe "unloadingGuidance.notNewAuth.heading"
      viewModel.heading(newAuth = false, goodsTooLarge = true) mustBe "unloadingGuidance.notNewAuth.heading"
    }

    "return correct heading for newAuth and goodsTooLarge = false" in {
      viewModel.heading(newAuth = true, goodsTooLarge = false) mustBe "unloadingGuidance.newAuth.goodsTooLargeNo.heading"
    }

    "return correct heading for newAuth and goodsTooLarge = true" in {
      viewModel.heading(newAuth = true, goodsTooLarge = true) mustBe "unloadingGuidance.newAuth.goodsTooLargeYes.heading"
    }

    "return correct preLinkText for newAuth and goodsTooLarge = false" in {
      viewModel.preLinkText(newAuth = true, goodsTooLarge = false) mustBe "unloadingGuidance.preLinkText"
    }

    "return empty preLinkText for not newAuth" in {
      viewModel.preLinkText(newAuth = false, goodsTooLarge = false) mustBe ""
      viewModel.preLinkText(newAuth = false, goodsTooLarge = true) mustBe ""
    }

    "return empty preLinkText for newAuth and goodsTooLarge = true" in {
      viewModel.preLinkText(newAuth = true, goodsTooLarge = true) mustBe ""
    }

    "return correct postLinkText for newAuth and goodsTooLarge = false" in {
      viewModel.postLinkText(newAuth = true, goodsTooLarge = false) mustBe "unloadingGuidance.postLinkText"
    }

    "return empty postLinkText for not newAuth" in {
      viewModel.postLinkText(newAuth = false, goodsTooLarge = false) mustBe ""
      viewModel.postLinkText(newAuth = false, goodsTooLarge = true) mustBe ""
    }

    "return empty postLinkText for newAuth and goodsTooLarge = true" in {
      viewModel.postLinkText(newAuth = true, goodsTooLarge = true) mustBe ""
    }

    "return correct para1 for newAuth and goodsTooLarge = true" in {
      viewModel.para1(newAuth = true, goodsTooLarge = true) mustBe "unloadingGuidance.para1"
    }

    "return empty para1 for not newAuth" in {
      viewModel.para1(newAuth = false, goodsTooLarge = false) mustBe ""
      viewModel.para1(newAuth = false, goodsTooLarge = true) mustBe ""
    }

    "return empty para1 for newAuth and goodsTooLarge = false" in {
      viewModel.para1(newAuth = true, goodsTooLarge = false) mustBe ""
    }

    "return correct para2 for not newAuth" in {
      viewModel.para2(newAuth = false, goodsTooLarge = false) mustBe "unloadingGuidance.para2.notNewAuth"
      viewModel.para2(newAuth = false, goodsTooLarge = true) mustBe "unloadingGuidance.para2.notNewAuth"
    }

    "return empty para2 for newAuth and goodsTooLarge = false" in {
      viewModel.para2(newAuth = true, goodsTooLarge = false) mustBe ""
    }

    "return correct para2 for newAuth and goodsTooLarge = true" in {
      viewModel.para2(newAuth = true, goodsTooLarge = true) mustBe "unloadingGuidance.para2.newAuth.goodsTooLargeYes"
    }

  }

}
