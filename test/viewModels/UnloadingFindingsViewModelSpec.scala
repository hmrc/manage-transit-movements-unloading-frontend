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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import viewModels.UnloadingFindingsViewModel.UnloadingFindingsViewModelProvider
import viewModels.sections.Section

class UnloadingFindingsViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "Unloading findings sections" - {
    "must have correct titles" in {

      setExistingUserAnswers(ie043UserAnswers)

      val viewModelProvider         = new UnloadingFindingsViewModelProvider().apply(ie043UserAnswers)
      val sections                  = viewModelProvider.section
      val additionalCommentsSection = viewModelProvider.additionalComments

      sections.head.sectionTitle.value mustBe "Means of transport"
      sections(1).sectionTitle.value mustBe "Transport equipment 1"
      sections(2).sectionTitle.value mustBe "Items summary"
      sections(3).sectionTitle.value mustBe "Item 1"
      additionalCommentsSection.sectionTitle.value mustBe "Additional comments"

    }
    
    "must render Means of Transport section" in {
      
      val userAnswers = emptyUserAnswers
        .setValue()
      
      
    }
  }

}
