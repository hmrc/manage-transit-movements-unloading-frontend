/*
 * Copyright 2022 HM Revenue & Customs
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
import models.{NormalMode, Seal}
import queries.SealsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewModels.sections.Section

class SealsSectionSpec extends SpecBase {

  private val mode = NormalMode

  "SealsSection" - {

    "return rows" - {

      "when there are seals" in {
        val seals = Seq(
          Seal("new seal value", removable = true),
          Seal("existing seal value", removable = false)
        )

        val userAnswers = emptyUserAnswers.setValue(SealsQuery, seals)

        val section: Section = SealsSection(userAnswers, mode)
        section.sectionTitle.get mustBe "Official customs seals"
        section.rows.head.value.content mustBe Text("existing seal value")
        section.rows.head.actions.get.items.size mustBe 1
        section.rows(1).value.content mustBe Text("new seal value")
        section.rows(1).actions.get.items.size mustBe 2
      }
    }

    "return no rows when no seals exist" in {
      val section: Section = SealsSection(emptyUserAnswers, mode)
      section.sectionTitle.get mustBe "Official customs seals"
      section.rows mustBe empty
    }
  }
}
