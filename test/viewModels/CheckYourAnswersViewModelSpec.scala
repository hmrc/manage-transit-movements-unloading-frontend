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
import models.Index
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider
import viewModels.UnloadingFindingsViewModel.UnloadingFindingsViewModelProvider

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "headerSection" - {

    "must render rows" in {

      val unloadedDate = arbitrary[LocalDate].sample.value
      val userAnswers = emptyUserAnswers
        .setValue(DateGoodsUnloadedPage, unloadedDate)
        .setValue(CanSealsBeReadPage, true)
        .setValue(AreAnySealsBrokenPage, true)

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new CheckYourAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.sections.length mustBe 2
      result.sections.head.rows.size mustBe 3
    }

  }
  "comments section" - {

    "must render 1 row if comments completed" in {

      val userAnswers = emptyUserAnswers
        .setValue(UnloadingCommentsPage, "Test")

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new CheckYourAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.sections.length mustBe 2
      result.sections(1).rows.size mustBe 1

      result.sections(1).sectionTitle.value mustBe "Additional comments"

    }

  }
}
