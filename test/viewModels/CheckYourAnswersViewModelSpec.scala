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
import models.{TraderAtDestination, UnloadingPermission, UserAnswers}
import pages._
import uk.gov.hmrc.viewmodels.Text.Literal

import java.time.LocalDate

class CheckYourAnswersViewModelSpec extends SpecBase {

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

  private val unloadingPermissionWithTransport = UnloadingPermission(
    movementReferenceNumber = "19IT02110010007827",
    transportIdentity = Some("YK67 XPF"),
    transportCountry = Some("United Kingdom"),
    numberOfItems = 1,
    numberOfPackages = Some(1),
    grossMass = "1000",
    traderAtDestination = TraderAtDestination("eori", "name", "streetAndNumber", "postcode", "city", "countryCode"),
    presentationOffice = "GB000060",
    seals = None,
    goodsItems = NonEmptyList(goodsItemMandatory, Nil),
    dateOfPreparation = LocalDate.now()
  )

  private val transportCountry = None

  "CheckYourAnswersViewModel" - {

    "contain date goods unloaded" in {

      val date                     = LocalDate.of(2020: Int, 3: Int, 12: Int)
      val userAnswers: UserAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, date).success.value
      val data                     = CheckYourAnswersViewModel(userAnswers, unloadingPermission, transportCountry)

      data.sections.length mustBe 3
      data.sections.head.rows.head.value.content mustBe Literal("12 March 2020")
    }
    "contain vehicle registration details with new user answers" in {
      val userAnswers = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, "vehicle reference").success.value
      val data        = CheckYourAnswersViewModel(userAnswers, unloadingPermission, transportCountry)

      data.sections.length mustBe 3
      data.sections(2).rows.head.value.content mustBe Literal("vehicle reference")
      data.sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain transport country details from unloading permission" in {
      val data = CheckYourAnswersViewModel(emptyUserAnswers, unloadingPermissionWithTransport, transportCountry)

      data.sections.length mustBe 3
      data.sections(2).rows(1).value.content mustBe Literal("United Kingdom")
    }
    "contain gross mass amount details from unloading permission" in {
      val data = CheckYourAnswersViewModel(emptyUserAnswers, unloadingPermission, transportCountry)

      data.sections.length mustBe 3
      data.sections(2).rows.head.value.content mustBe Literal("1000")
      data.sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain gross mass details" in {
      val userAnswers = emptyUserAnswers.set(GrossMassAmountPage, "500").success.value
      val data        = CheckYourAnswersViewModel(userAnswers, unloadingPermission, transportCountry)

      data.sections.length mustBe 3
      data.sections(2).rows.head.value.content mustBe Literal("500")
      data.sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain number of items details" in {
      val userAnswers = emptyUserAnswers.set(TotalNumberOfItemsPage, 10).success.value
      val data        = CheckYourAnswersViewModel(userAnswers, unloadingPermission, transportCountry)

      data.sections.length mustBe 3
      data.sections(2).rows(1).value.content mustBe Literal("10")
      data.sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain number of items details with details from unloading permission" in {
      val data = CheckYourAnswersViewModel(emptyUserAnswers, unloadingPermission, transportCountry)

      data.sections.length mustBe 3
      data.sections(2).rows(1).value.content mustBe Literal("1")
      data.sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain number of packages details with details from unloading permission" in {
      val data = CheckYourAnswersViewModel(emptyUserAnswers, unloadingPermission, transportCountry)

      data.sections.length mustBe 3
      data.sections(2).rows(2).value.content mustBe Literal("1")
      data.sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain number of packages details" in {
      val userAnswers = emptyUserAnswers.set(TotalNumberOfPackagesPage, 11).success.value
      val data        = CheckYourAnswersViewModel(userAnswers, unloadingPermission, transportCountry)

      data.sections.length mustBe 3
      data.sections(2).rows(2).value.content mustBe Literal("11")
      data.sections(2).rows.head.actions.isEmpty mustBe false
    }
    "contain item details" in {
      val userAnswers = emptyUserAnswers
      val data        = CheckYourAnswersViewModel(userAnswers, unloadingPermission, transportCountry)

      data.sections.length mustBe 3
      data.sections(2).rows(3).value.content mustBe Literal("Flowers")
    }
    "contain comments details" in {
      val userAnswers = emptyUserAnswers.set(ChangesToReportPage, "Test comment").success.value
      val data        = CheckYourAnswersViewModel(userAnswers, unloadingPermission, transportCountry)

      data.sections.length mustBe 3
      data.sections(2).rows(4).value.content mustBe Literal("Test comment")
    }
  }
}
