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
import models.reference.Country
import models.{TraderAtDestination, UnloadingPermission}
import pages.{VehicleNameRegistrationReferencePage, VehicleRegistrationCountryPage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewModels.sections.Section

import java.time.LocalDate

class TransportSectionSpec extends SpecBase {

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

  "TransportSection" - {

    "when values have not been changed must display" - {

      "correct transport identity number from unloading permission" in {

        val unloadingPermission = sampleUnloadingPermission.copy(transportIdentity = Some("RegNumber1"))
        val section: Section    = TransportSection(emptyUserAnswers, None, unloadingPermission).get
        section.rows.head.value.content mustBe Text("RegNumber1")
      }

      "correct transport country from unloading permission " in {

        val unloadingPermission = sampleUnloadingPermission.copy(transportCountry = Some("France"))
        val section: Section    = TransportSection(emptyUserAnswers, None, unloadingPermission).get
        section.rows.head.value.content mustBe Text("France")
      }

      "correct country from Country object" in {

        val unloadingPermission = sampleUnloadingPermission.copy(transportCountry = Some("FR"))
        val section: Section    = TransportSection(emptyUserAnswers, Some(Country("FR", "France")), unloadingPermission).get
        section.rows.head.value.content mustBe Text("France")
      }

      "no sections if identity and country don't exist" in {

        val unloadingPermission      = sampleUnloadingPermission.copy(transportCountry = None, transportIdentity = None)
        val section: Option[Section] = TransportSection(emptyUserAnswers, None, unloadingPermission)
        section mustBe None
      }

    }

    "when values have been changed must display" - {

      "display correct transport identity when change has been made" in {

        val unloadingPermission = sampleUnloadingPermission.copy(transportIdentity = Some("RegNumber1"))

        val updatedUserAnswers = emptyUserAnswers
          .setValue(VehicleNameRegistrationReferencePage, "RegNumber2")

        val section: Section = TransportSection(updatedUserAnswers, None, unloadingPermission).get
        section.rows.head.value.content mustBe Text("RegNumber2")
      }

      "correct transport vehicle registration country when change has been made" in {

        val unloadingPermission = sampleUnloadingPermission.copy(transportCountry = Some("United Kingdom"))

        val updatedUserAnswers = emptyUserAnswers
          .setValue(VehicleRegistrationCountryPage, Country("FR", "France"))

        val section: Section = TransportSection(updatedUserAnswers, None, unloadingPermission).get
        section.rows.head.value.content mustBe Text("France")
      }
    }

  }
}
