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
import models.{Index, Seals, TraderAtDestination, UnloadingPermission}
import pages.NewSealNumberPage
import uk.gov.hmrc.viewmodels.Text.Literal
import utils.UnloadingSummaryHelper
import viewModels.sections.Section

import java.time.LocalDate

class SealsSectionSpec extends SpecBase {

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

  "SealsSection" - {

    "contain data from unloading permission" in {

      val withSeals = unloadingPermission.copy(seals = Some(Seals(1, Seq("seal 1", "seal 2"))))

      val data: Seq[Section] = SealsSection(emptyUserAnswers)(withSeals, new UnloadingSummaryHelper(emptyUserAnswers)).head
      data.head.rows(0).value.content mustBe Literal("seal 1")
      data.head.rows(1).value.content mustBe Literal("seal 2")
    }

    "contain data from user answers" in {

      val withSeals = unloadingPermission.copy(seals = Some(Seals(1, Seq("seal 1", "seal 2"))))

      val updatedUserAnswers = emptyUserAnswers
        .set(NewSealNumberPage(Index(0)), "new seal value 1")
        .success
        .value
        .set(NewSealNumberPage(Index(1)), "new seal value 2")
        .success
        .value

      val data: Seq[Section] = SealsSection(updatedUserAnswers)(withSeals, new UnloadingSummaryHelper(updatedUserAnswers)).head
      data.head.rows(0).value.content mustBe Literal("new seal value 1")
      data.head.rows(1).value.content mustBe Literal("new seal value 2")
    }

    "return nothing if no seals exist" in {

      val noSeals = unloadingPermission.copy(seals = None)

      val data: Option[Seq[Section]] = SealsSection(emptyUserAnswers)(noSeals, new UnloadingSummaryHelper(emptyUserAnswers))
      data mustBe None
    }

  }

}
