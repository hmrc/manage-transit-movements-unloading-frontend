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
import models.reference.Country
import pages.{VehicleIdentificationNumberPage, VehicleRegistrationCountryPage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewModels.sections.Section

class TransportSectionSpec extends SpecBase {

  private val mode = NormalMode

  "TransportSection" - {

    "must display" - {

      "correct transport identity number" in {
        val userAnswers      = emptyUserAnswers.setValue(VehicleIdentificationNumberPage, "RegNumber1")
        val section: Section = TransportSection(userAnswers, mode)
        section.sectionTitle.get mustBe "Vehicle used"
        section.rows.head.value.content mustBe Text("RegNumber1")
      }

      "correct transport country from unloading permission " in {
        val userAnswers      = emptyUserAnswers.setValue(VehicleRegistrationCountryPage, Country("FR", "France"))
        val section: Section = TransportSection(userAnswers, mode)
        section.sectionTitle.get mustBe "Vehicle used"
        section.rows.head.value.content mustBe Text("France")
      }

      "nothing when user answers empty" in {
        val section: Section = TransportSection(emptyUserAnswers, mode)
        section.sectionTitle.get mustBe "Vehicle used"
        section.rows mustBe empty
      }
    }
  }
}
