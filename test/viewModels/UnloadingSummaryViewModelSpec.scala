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
import models.reference.Country
import models.{Index, NormalMode, Seal}
import pages._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class UnloadingSummaryViewModelSpec extends SpecBase {

  private val mode = NormalMode

  "UnloadingSummaryViewModel" - {

    "seals sections should" - {
      "display no rows when there are no TotalNumberOfPackagesControllerSpec in user answers" in {
        val section = new UnloadingSummaryViewModel().sealsSection(emptyUserAnswers, mode)

        section.sectionTitle.get mustBe "Official customs seals"
        section.rows mustBe empty
      }

      "display rows when there are seals in user answers" in {
        val userAnswers = emptyUserAnswers
          .setValue(SealPage(Index(0)), Seal("seal 1", removable = false))
          .setValue(SealPage(Index(1)), Seal("seal 2", removable = true))

        val section = new UnloadingSummaryViewModel().sealsSection(userAnswers, mode)

        section.sectionTitle.get mustBe "Official customs seals"
        section.rows.length mustBe 2
      }
    }

    "vehicle sections should" - {

      "display transportIdentity" in {
        val userAnswers = emptyUserAnswers.setValue(VehicleIdentificationNumberPage, "registration")

        val sections = new UnloadingSummaryViewModel().transportAndItemSections(userAnswers, mode)

        sections.length mustBe 2
        sections.head.sectionTitle.get mustBe "Vehicle used"
        sections.head.rows.length mustBe 1
      }

      "display transportCountry" in {
        val userAnswers = emptyUserAnswers.setValue(VehicleRegistrationCountryPage, Country("FR", "France"))

        val sections = new UnloadingSummaryViewModel().transportAndItemSections(userAnswers, mode)

        sections.length mustBe 2
        sections.head.sectionTitle.get mustBe "Vehicle used"
        sections.head.rows.length mustBe 1
      }

      "display transportCountry and transportIdentity" in {
        val userAnswers = emptyUserAnswers
          .setValue(VehicleIdentificationNumberPage, "registration")
          .setValue(VehicleRegistrationCountryPage, Country("FR", "France"))

        val sections = new UnloadingSummaryViewModel().transportAndItemSections(userAnswers, mode)

        sections.length mustBe 2
        sections.head.sectionTitle.get mustBe "Vehicle used"
        sections.head.rows.length mustBe 2
      }
    }

    "items sections should" - {

      "display total mass with single item" in {
        val userAnswers = emptyUserAnswers.setValue(GrossWeightPage, "99")

        val sections = new UnloadingSummaryViewModel().transportAndItemSections(userAnswers, mode)

        sections.length mustBe 2
        sections(1).sectionTitle.get mustBe "Items"
        sections(1).rows.length mustBe 1
        sections(1).rows.head.value.content mustBe Text("99")
      }

      "display total number of items " in {
        val userAnswers = emptyUserAnswers.setValue(TotalNumberOfItemsPage, 8)

        val sections = new UnloadingSummaryViewModel().transportAndItemSections(userAnswers, mode)

        sections.length mustBe 2
        sections(1).sectionTitle.get mustBe "Items"
        sections(1).rows.length mustBe 1
        sections(1).rows.head.value.content mustBe Text("8")
      }

      "contain number of packages details " in {
        val userAnswers = emptyUserAnswers.setValue(TotalNumberOfPackagesPage, "11")

        val sections = new UnloadingSummaryViewModel().transportAndItemSections(userAnswers, mode)

        sections.length mustBe 2
        sections(1).sectionTitle.get mustBe "Items"
        sections(1).rows.length mustBe 1
        sections(1).rows.head.value.content mustBe Text("11")
      }
    }
  }
}
