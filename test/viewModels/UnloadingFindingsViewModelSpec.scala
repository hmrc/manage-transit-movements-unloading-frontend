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
  private val mockUnloadingFindingsViewModelProvider = mock[UnloadingFindingsViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[UnloadingFindingsViewModelProvider].toInstance(mockUnloadingFindingsViewModelProvider))

  "Unloading findings sections" - {
    "must have correct titles" in {
      setExistingUserAnswers(ie043UserAnswers)
      val itemsSummaryTitle            = "Items summary"
      val itemsOneTitle                = "Item 1"
      val meansOfTransportSectionTitle = "Means of transport"
      val transportEquipmentTitle      = "Transport equipment 1"
      val additionalCommentsTitle      = "Additional comments"

      when(mockUnloadingFindingsViewModelProvider.apply(any())(any()))
        .thenReturn(UnloadingFindingsViewModel(Nil, ))

      val section: Seq[Section] = new UnloadingFindingsViewModelProvider().apply(ie043UserAnswers).section

      section.section.head.sectionTitle.value mustBe meansOfTransportSectionTitle
      section.section(1).sectionTitle.value mustBe transportEquipmentTitle
      section.section(2).sectionTitle.value mustBe itemsSummaryTitle
      section.section(3).sectionTitle.value mustBe itemsOneTitle
      section.section(4).sectionTitle.value mustBe additionalCommentsTitle

    }
  }

}
