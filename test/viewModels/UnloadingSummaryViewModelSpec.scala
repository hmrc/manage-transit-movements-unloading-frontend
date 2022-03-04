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
import models.{Seals, TraderAtDestination, UnloadingPermission}
import pages.{GrossMassAmountPage, TotalNumberOfItemsPage, TotalNumberOfPackagesPage}
import uk.gov.hmrc.viewmodels.Text.Literal

import java.time.LocalDate

class UnloadingSummaryViewModelSpec extends SpecBase {

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

  private val transportCountry = None

  "UnloadingSummaryViewModel" - {

    "seals sections should" - {
      "display no seals" in {

        val data = UnloadingSummaryViewModel(emptyUserAnswers, transportCountry)(unloadingPermission)

        data.sections.length mustBe 1
      }

      "display seals" in {

        val withSeals = unloadingPermission.copy(seals = Some(Seals(1, Seq("seal 1", "seal 2"))))

        val data = UnloadingSummaryViewModel(emptyUserAnswers, transportCountry)(withSeals)

        data.sections.length mustBe 1
        data.sections.head.sectionTitle mustBe defined
        data.sections.head.rows.length mustBe 4
      }

      "display seals with transport details" in {

        val withSeals = unloadingPermission.copy(seals = Some(Seals(1, Seq("seal 1", "seal 2"))),
                                                 transportCountry = Some("registration"),
                                                 transportIdentity = Some("registration")
        )

        val data = UnloadingSummaryViewModel(emptyUserAnswers, transportCountry)(withSeals)

        data.sections.length mustBe 2
        data.sections(0).sectionTitle mustBe defined
        data.sections(0).rows.length mustBe 2
        data.sections(1).sectionTitle mustBe defined
        data.sections(1).rows.length mustBe 4
      }
    }

    "vehicle sections should" - {

      "display transportIdentity" in {

        val transportIdentity = unloadingPermission.copy(transportIdentity = Some("registration"))

        val data = UnloadingSummaryViewModel(emptyUserAnswers, transportCountry)(transportIdentity)

        data.sections.length mustBe 2
        data.sections.head.sectionTitle mustBe defined
        data.sections.head.rows.length mustBe 1
      }

      "display transportCountry" in {

        val unloadingPermissionTransportCountry = unloadingPermission.copy(transportCountry = Some("registration"))

        val data = UnloadingSummaryViewModel(emptyUserAnswers, transportCountry)(unloadingPermissionTransportCountry)

        data.sections.length mustBe 2
        data.sections.head.sectionTitle mustBe defined
        data.sections.head.rows.length mustBe 1
      }

      "display transportCountry and transportIdentity" in {

        val unloadingPermissionTransportCountry = unloadingPermission.copy(transportCountry = Some("registration"), transportIdentity = Some("registration"))

        val data = UnloadingSummaryViewModel(emptyUserAnswers, transportCountry)(unloadingPermissionTransportCountry)

        data.sections.length mustBe 2
        data.sections.head.sectionTitle mustBe defined
        data.sections.head.rows.length mustBe 2
      }
    }

    "items sections should" - {

      "display total mass with single item" in {

        val userAnswers = emptyUserAnswers.set(GrossMassAmountPage, "99").success.value
        val data        = UnloadingSummaryViewModel(userAnswers, transportCountry)(unloadingPermission)

        data.sections.head.rows.head.value.content mustBe Literal("99")
        data.sections.length mustBe 1
        data.sections.head.sectionTitle mustBe defined
        data.sections.head.rows.length mustBe 4
        data.sections.head.rows.head.actions.isEmpty mustBe false
        data.sections.head.rows(3).actions.isEmpty mustBe true
      }

      "display total number of items " in {
        val userAnswers = emptyUserAnswers.set(TotalNumberOfItemsPage, 8).success.value

        val data: UnloadingSummaryViewModel = UnloadingSummaryViewModel(userAnswers, transportCountry)(unloadingPermission)

        data.sections.length mustBe 1
        data.sections.head.rows(1).value.content mustBe Literal("8")
        data.sections.head.rows.head.actions.isEmpty mustBe false
      }

      "contain number of packages details " in {
        val userAnswers = emptyUserAnswers.set(TotalNumberOfPackagesPage, 11).success.value
        val data        = UnloadingSummaryViewModel(userAnswers, transportCountry)(unloadingPermission)

        data.sections.length mustBe 1
        data.sections.head.rows(2).value.content mustBe Literal("11")
        data.sections.head.rows.head.actions.isEmpty mustBe false
      }

    }

  }

}
