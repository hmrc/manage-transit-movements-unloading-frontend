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
import utils.Format.booleanToIntWrites
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider

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

    "must render 1 rows if add comments false" in {

      val userAnswers = emptyUserAnswers
        .setValue(AddUnloadingCommentsYesNoPage, false)(booleanToIntWrites)

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new CheckYourAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.sections.length mustBe 2
      result.sections(1).rows.size mustBe 1

      result.sections(1).sectionTitle.value mustBe "What you found when unloading"

    }

    "must render 2 rows if add comments true" in {

      val userAnswers = emptyUserAnswers
        .setValue(AddUnloadingCommentsYesNoPage, true)(booleanToIntWrites)
        .setValue(UnloadingCommentsPage, "Test")

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new CheckYourAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.sections.length mustBe 2
      result.sections(1).rows.size mustBe 2

      result.sections(1).sectionTitle.value mustBe "What you found when unloading"

    }

  }
}
