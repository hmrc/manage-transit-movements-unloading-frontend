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
import models.NormalMode
import pages.{GrossWeightPage, TotalNumberOfItemsPage, TotalNumberOfPackagesPage, UnloadingCommentsPage}
import queries.GoodsItemsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewModels.sections.Section

class ItemsSectionSpec extends SpecBase {

  private val mode = NormalMode

  "ItemsSection" - {
    "must display" - {

      "no rows if answers are empty" in {
        val section: Section = ItemsSection(emptyUserAnswers, mode)
        section.sectionTitle.get mustBe "Items"
        section.rows mustBe empty
      }

      "correct gross mass" in {
        val userAnswers      = emptyUserAnswers.setValue(GrossWeightPage, "1000")
        val section: Section = ItemsSection(userAnswers, mode)
        section.sectionTitle.get mustBe "Items"
        section.rows.head.value.content mustBe Text("1000")
      }

      "correct number of items" in {
        val userAnswers      = emptyUserAnswers.setValue(TotalNumberOfItemsPage, 10)
        val section: Section = ItemsSection(userAnswers, mode)
        section.sectionTitle.get mustBe "Items"
        section.rows.head.value.content mustBe Text("10")
      }

      "correct number of packages" in {
        val userAnswers      = emptyUserAnswers.setValue(TotalNumberOfPackagesPage, "10")
        val section: Section = ItemsSection(userAnswers, mode)
        section.sectionTitle.get mustBe "Items"
        section.rows.head.value.content mustBe Text("10")
      }

      "correct items" in {
        val userAnswers      = emptyUserAnswers.setValue(GoodsItemsQuery, Seq("Test"))
        val section: Section = ItemsSection(userAnswers, mode)
        section.sectionTitle.get mustBe "Items"
        section.rows.head.value.content mustBe Text("Test")
      }

      "correct comments" in {
        val userAnswers      = emptyUserAnswers.setValue(UnloadingCommentsPage, "Test")
        val section: Section = ItemsSection(userAnswers, mode)
        section.sectionTitle.get mustBe "Items"
        section.rows.head.value.content mustBe Text("Test")
      }
    }
  }
}
