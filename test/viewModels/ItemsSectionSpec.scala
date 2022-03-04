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
import uk.gov.hmrc.viewmodels.Text.Literal
import utils.UnloadingSummaryHelper
import viewModels.sections.Section

import java.time.LocalDate

class ItemsSectionSpec extends SpecBase {

  val unloadingPermission: UnloadingPermission = UnloadingPermission(
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

        val grossMassAmount    = unloadingPermission.copy(grossMass = "1000")
        val data: Seq[Section] = ItemsSection(emptyUserAnswers)(grossMassAmount, new UnloadingSummaryHelper(emptyUserAnswers))
        data.head.rows.head.value.content mustBe Literal("1000")
        data.head.rows(3).value.content mustBe Literal("Flowers")
      }
      "Correct number of items when no changes have been made" in {

        val numberOfItems      = unloadingPermission.copy(grossMass = "1000", numberOfItems = 10)
        val data: Seq[Section] = ItemsSection(emptyUserAnswers)(numberOfItems, new UnloadingSummaryHelper(emptyUserAnswers))
        data.head.rows(1).value.content mustBe Literal("10")
      }

      "Correct Gross mass when change has been made" in {
        val grossMassAmount = unloadingPermission.copy(grossMass = "1000")

        val updatedAnswers = emptyUserAnswers
          .set(GrossMassAmountPage, "2000")
          .success
          .value

        val data: Seq[Section] = ItemsSection(updatedAnswers)(grossMassAmount, new UnloadingSummaryHelper(emptyUserAnswers))
        data.head.rows.head.value.content mustBe Literal("2000")
        data.head.rows(3).value.content mustBe Literal("Flowers")
      }

      "Correct Comments when change has been made" in {
        val updatedAnswers = emptyUserAnswers
          .set(ChangesToReportPage, "Test")
          .success
          .value

        val data: Seq[Section] = ItemsSection(updatedAnswers)(unloadingPermission, new UnloadingSummaryHelper(emptyUserAnswers))
        data.head.rows(4).value.content mustBe Literal("Test")
      }

    }
  }

}
