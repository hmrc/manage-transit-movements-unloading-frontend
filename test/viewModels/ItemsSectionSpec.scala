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
import cats.data.NonEmptyList
import models.{TraderAtDestination, UnloadingPermission}
import pages.{ChangesToReportPage, GrossMassAmountPage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewModels.sections.Section

import java.time.LocalDate

class ItemsSectionSpec extends SpecBase {

  val sampleUnloadingPermission: UnloadingPermission = UnloadingPermission(
    movementReferenceNumber = "19IT02110010007827",
    transportIdentity = None,
    transportCountry = None,
    grossMass = "1000",
    numberOfItems = 1,
    numberOfPackages = Some(1),
    traderAtDestination = TraderAtDestination("eori", "name", "streetAndNumber", "postcode", "city", "countryCode"),
    presentationOffice = "GB000060",
    seals = None,
    goodsItems = NonEmptyList(goodsItemMandatory, Nil),
    dateOfPreparation = LocalDate.now()
  )

  "ItemsSection" - {
    "Must display" - {
      "Correct Gross mass when no changes have been made" in {

        val unloadingPermission = sampleUnloadingPermission.copy(grossMass = "1000")
        val section: Section    = ItemsSection(emptyUserAnswers, unloadingPermission)
        section.rows.head.value.content mustBe Text("1000")
        section.rows(3).value.content mustBe Text("Flowers")
      }
      "Correct number of items when no changes have been made" in {

        val unloadingPermission = sampleUnloadingPermission.copy(grossMass = "1000", numberOfItems = 10)
        val section: Section    = ItemsSection(emptyUserAnswers, unloadingPermission)
        section.rows(1).value.content mustBe Text("10")
      }

      "Correct Gross mass when change has been made" in {
        val unloadingPermission = sampleUnloadingPermission.copy(grossMass = "1000")

        val updatedAnswers = emptyUserAnswers
          .setValue(GrossMassAmountPage, "2000")

        val section: Section = ItemsSection(updatedAnswers, unloadingPermission)
        section.rows.head.value.content mustBe Text("2000")
        section.rows(3).value.content mustBe Text("Flowers")
      }

      "Correct Comments when change has been made" in {
        val updatedAnswers = emptyUserAnswers
          .setValue(ChangesToReportPage, "Test")

        val section: Section = ItemsSection(updatedAnswers, sampleUnloadingPermission)
        section.rows(4).value.content mustBe Text("Test")
      }

    }
  }

}
