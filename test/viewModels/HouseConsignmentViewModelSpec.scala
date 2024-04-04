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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.Index
import models.reference.{AdditionalInformationCode, Country, TransportMeansIdentification}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.houseConsignment.index.additionalinformation.{HouseConsignmentAdditionalInformationCodePage, HouseConsignmentAdditionalInformationTextPage}
import pages.houseConsignment.index.items.{
  GrossWeightPage,
  ItemDescriptionPage,
  NetWeightPage,
  ConsigneeIdentifierPage => ItemConsigneeIdentifierPage,
  ConsigneeNamePage => ItemConsigneeNamePage
}
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import viewModels.HouseConsignmentViewModel.HouseConsignmentViewModelProvider
import viewModels.sections.Section.{AccordionSection, StaticSection}

class HouseConsignmentViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val country                     = Country("GB", "United Kingdom")
  private val vehicleIdentificationNumber = "12324"
  private val identificationType          = TransportMeansIdentification("51", "description")

  "HouseConsignmentViewModel" - {

    "must render Means of Transport section" - {
      "when there is one" in {

        val answers = emptyUserAnswers
          .setValue(DepartureTransportMeansIdentificationNumberPage(Index(0), index), vehicleIdentificationNumber)
          .setValue(DepartureTransportMeansIdentificationTypePage(Index(0), index), identificationType)
          .setValue(DepartureTransportMeansCountryPage(Index(0), index), country)

        setExistingUserAnswers(answers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.sections.head

        section.sectionTitle.value mustBe "Departure means of transport"
        section.children.size mustBe 1

        section.children.head.sectionTitle.value mustBe "Departure means of transport 1"
        section.children.head.rows.size mustBe 3

        section.viewLinks mustBe Nil
      }

      "when there is multiple" in {

        val answers = emptyUserAnswers
          .setValue(DepartureTransportMeansIdentificationNumberPage(hcIndex, Index(0)), vehicleIdentificationNumber)
          .setValue(DepartureTransportMeansIdentificationTypePage(hcIndex, Index(0)), identificationType)
          .setValue(DepartureTransportMeansCountryPage(hcIndex, Index(0)), country)
          .setValue(DepartureTransportMeansIdentificationNumberPage(hcIndex, Index(1)), vehicleIdentificationNumber)
          .setValue(DepartureTransportMeansIdentificationTypePage(hcIndex, Index(1)), identificationType)
          .setValue(DepartureTransportMeansCountryPage(hcIndex, Index(1)), country)

        setExistingUserAnswers(answers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.sections.head

        section.sectionTitle.value mustBe "Departure means of transport"
        section.children.size mustBe 2

        section.children.head.sectionTitle.value mustBe "Departure means of transport 1"
        section.children.head.rows.size mustBe 3

        section.children(1).sectionTitle.value mustBe "Departure means of transport 2"
        section.children(1).rows.size mustBe 3

        section.viewLinks mustBe Nil
      }
    }

    "must render Additional Information section" - {
      val code        = arbitrary[AdditionalInformationCode].sample.value
      val description = nonEmptyString.sample.value
      "when there is one" in {

        val answers = emptyUserAnswers
          .setValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, Index(0)), code)
          .setValue(HouseConsignmentAdditionalInformationTextPage(hcIndex, Index(0)), description)

        setExistingUserAnswers(answers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.sections(1)

        section.sectionTitle.value mustBe "Additional information"
        section.children.size mustBe 1

        section.children.head.sectionTitle.value mustBe "Additional information 1"
        section.children.head.rows.size mustBe 2

        section.viewLinks mustBe Nil
      }

      "when there is multiple" in {

        val answers = emptyUserAnswers
          .setValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, Index(0)), code)
          .setValue(HouseConsignmentAdditionalInformationTextPage(hcIndex, Index(0)), description)
          .setValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, Index(1)), code)
          .setValue(HouseConsignmentAdditionalInformationTextPage(hcIndex, Index(1)), description)

        setExistingUserAnswers(answers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.sections(1)

        section.sectionTitle.value mustBe "Additional information"
        section.children.size mustBe 2

        section.children.head.sectionTitle.value mustBe "Additional information 1"
        section.children.head.rows.size mustBe 2

        section.children(1).sectionTitle.value mustBe "Additional information 2"
        section.children(1).rows.size mustBe 2

        section.viewLinks mustBe Nil
      }
    }

    "must render house consignment section" - {
      "when there is one" in {
        val userAnswers = emptyUserAnswers
          .setValue(ConsignorNamePage(hcIndex), "michael doe")
          .setValue(ConsignorIdentifierPage(hcIndex), "csgr1")
          .setValue(ConsigneeNamePage(hcIndex), "John Smith")
          .setValue(ConsigneeIdentifierPage(hcIndex), "csgee1")
          .setValue(ItemDescriptionPage(hcIndex, itemIndex), "shirts")
          .setValue(GrossWeightPage(hcIndex, itemIndex), BigDecimal(123.45))
          .setValue(NetWeightPage(hcIndex, itemIndex), 123.45)
          .setValue(ItemConsigneeNamePage(hcIndex, itemIndex), "John Smith")
          .setValue(ItemConsigneeIdentifierPage(hcIndex, itemIndex), "csgee2")

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers, index)

        result.sections.head mustBe a[AccordionSection]
        result.sections.head.sectionTitle.value mustBe "Departure means of transport"

        result.sections(2) mustBe a[AccordionSection]
        result.sections(2).sectionTitle.value mustBe "Items"
        result.sections(2).viewLinks must not be empty

        result.sections(2).children.head mustBe a[AccordionSection]
        result.sections(2).children.head.sectionTitle.value mustBe "Item 1"
        result.sections(2).children.head.rows.size mustBe 5

        result.sections(2).children.head.children.head mustBe a[AccordionSection]
        result.sections(2).children.head.children.head.sectionTitle.value mustBe "UN numbers"
        result.sections(2).children.head.children.head.rows.size mustBe 0

        result.sections(2).children.head.children(1) mustBe a[StaticSection]
        result.sections(2).children.head.children(1).sectionTitle.value mustBe "Consignee"
        result.sections(2).children.head.children(1).rows.size mustBe 2

        result.sections(2).children.head.children(2) mustBe a[AccordionSection]
        result.sections(2).children.head.children(2).sectionTitle.value mustBe "Documents"
        result.sections(2).children.head.children(2).viewLinks.head.id mustBe "add-remove-item-1-document"
      }
    }

    "must render item section" - {
      "when there is one" in {
        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(hcIndex, itemIndex), "shirts")
          .setValue(GrossWeightPage(hcIndex, itemIndex), BigDecimal(123.45))
          .setValue(NetWeightPage(hcIndex, itemIndex), 123.45)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers, index)
        val section           = result.sections(2)

        section.sectionTitle.value mustBe "Items"
        section.children.head.sectionTitle.value mustBe "Item 1"
        section.children.head.rows.size mustBe 5
      }

      "when there are multiple" in {
        val userAnswers = emptyUserAnswers
          .setValue(ConsignorNamePage(hcIndex), "michael doe")
          .setValue(ConsignorIdentifierPage(hcIndex), "csgr1")
          .setValue(ConsigneeNamePage(hcIndex), "John Smith")
          .setValue(ConsigneeIdentifierPage(hcIndex), "csgee1")
          .setValue(ItemDescriptionPage(hcIndex, Index(0)), "shirts")
          .setValue(GrossWeightPage(hcIndex, Index(0)), BigDecimal(123.45))
          .setValue(NetWeightPage(hcIndex, Index(0)), 123.45)
          .setValue(ItemDescriptionPage(hcIndex, Index(1)), "shirts")
          .setValue(GrossWeightPage(hcIndex, Index(1)), BigDecimal(123.45))
          .setValue(NetWeightPage(hcIndex, Index(1)), 123.45)
          .setValue(ItemConsigneeNamePage(hcIndex, Index(0)), "John Smith")
          .setValue(ItemConsigneeIdentifierPage(hcIndex, Index(0)), "csgee2")
          .setValue(ItemConsigneeNamePage(hcIndex, Index(1)), "John Smith")
          .setValue(ItemConsigneeIdentifierPage(hcIndex, Index(1)), "csgee3")

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers, index)

        result.sections(2) mustBe a[AccordionSection]
        result.sections(2).sectionTitle.value mustBe "Items"
        result.sections(2).viewLinks must not be empty

        result.sections(2).children.head mustBe a[AccordionSection]
        result.sections(2).children.head.sectionTitle.value mustBe "Item 1"
        result.sections(2).children.head.rows.size mustBe 5

        result.sections(2).children(1) mustBe a[AccordionSection]
        result.sections(2).children(1).sectionTitle.value mustBe "Item 2"
        result.sections(2).children(1).rows.size mustBe 5

        result.sections(2).children.head.children.head mustBe a[AccordionSection]
        result.sections(2).children.head.children.head.sectionTitle.value mustBe "UN numbers"
        result.sections(2).children.head.children.head.rows.size mustBe 0

        result.sections(2).children.head.children(1) mustBe a[StaticSection]
        result.sections(2).children.head.children(1).sectionTitle.value mustBe "Consignee"
        result.sections(2).children.head.children(1).rows.size mustBe 2
      }
    }
  }
}
