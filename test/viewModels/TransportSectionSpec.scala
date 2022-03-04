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
import models.reference.Country
import pages.{VehicleNameRegistrationReferencePage, VehicleRegistrationCountryPage}
import uk.gov.hmrc.viewmodels.Text.Literal
import utils.UnloadingSummaryHelper
import viewModels.sections.Section

import java.time.LocalDate

class TransportSectionSpec extends SpecBase {

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

  "TransportSection" - {

    "when values have not been changed must display" - {

      "correct transport identity number from unloading permission" in {

        val regNumber          = unloadingPermission.copy(transportIdentity = Some("RegNumber1"))
        val data: Seq[Section] = TransportSection(emptyUserAnswers, None)(regNumber, new UnloadingSummaryHelper(emptyUserAnswers))
        data.head.rows.head.value.content mustBe Literal("RegNumber1")
      }

      "correct transport country from unloading permission " in {

        val regNumber          = unloadingPermission.copy(transportCountry = Some("France"))
        val data: Seq[Section] = TransportSection(emptyUserAnswers, None)(regNumber, new UnloadingSummaryHelper(emptyUserAnswers))
        data.head.rows.head.value.content mustBe Literal("France")
      }

      "correct country from Country object    " in {

        val regNumber          = unloadingPermission.copy(transportCountry = Some("FR"))
        val data: Seq[Section] = TransportSection(emptyUserAnswers, Some(Country("FR", "France")))(regNumber, new UnloadingSummaryHelper(emptyUserAnswers))
        data.head.rows.head.value.content mustBe Literal("France")
      }

      "no sections if identity and country don't exist" in {

        val noTransport        = unloadingPermission.copy(transportCountry = None, transportIdentity = None)
        val data: Seq[Section] = TransportSection(emptyUserAnswers, None)(noTransport, new UnloadingSummaryHelper(emptyUserAnswers))
        data mustBe Nil
      }

    }

    "when values have been changed must display" - {

      "display correct transport identity when change has been made" in {

        val regNumber = unloadingPermission.copy(transportIdentity = Some("RegNumber1"))

        val updatedUserAnswers = emptyUserAnswers
          .set(VehicleNameRegistrationReferencePage, "RegNumber2")
          .success
          .value

        val data: Seq[Section] = TransportSection(updatedUserAnswers, None)(regNumber, new UnloadingSummaryHelper(updatedUserAnswers))
        data.head.rows.head.value.content mustBe Literal("RegNumber2")
      }

      "correct transport vehicle registration country when change has been made" in {

        val regCountry = unloadingPermission.copy(transportCountry = Some("United Kingdom"))

        val updatedUserAnswers = emptyUserAnswers
          .set(VehicleRegistrationCountryPage, Country("FR", "France"))
          .success
          .value

        val data: Seq[Section] = TransportSection(updatedUserAnswers, None)(regCountry, new UnloadingSummaryHelper(updatedUserAnswers))
        data.head.rows.head.value.content mustBe Literal("France")
      }
    }

  }
}
