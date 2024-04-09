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
import models.DocType.{Support, Transport}
import models.{Index, SecurityType}
import models.reference.{Country, DocumentType, TransportMeansIdentification}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.houseConsignment.index.SecurityIndicatorFromExportDeclarationPage
import pages.houseConsignment.index.documents.{AdditionalInformationPage, DocumentReferenceNumberPage, TypePage}
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
        val section           = result.section.children.head

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
        val section           = result.section.children.head

        section.sectionTitle.value mustBe "Departure means of transport"
        section.children.size mustBe 2

        section.children.head.sectionTitle.value mustBe "Departure means of transport 1"
        section.children.head.rows.size mustBe 3

        section.children(1).sectionTitle.value mustBe "Departure means of transport 2"
        section.children(1).rows.size mustBe 3

        section.viewLinks mustBe Nil
      }
    }

    "must render Documents section" - {
      "when there is one" in {

        val answers = emptyUserAnswers
          .setValue(DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex), "ref1")
          .setValue(AdditionalInformationPage(houseConsignmentIndex, documentIndex), "additional info")
          .setValue(TypePage(houseConsignmentIndex, documentIndex), DocumentType(Transport, "code", "description"))

        setExistingUserAnswers(answers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.section.children(1)

        section.sectionTitle.value mustBe "Documents"
        section.children.head.sectionTitle.value mustBe "Document 1"
        section.children.head.rows.size mustBe 3

        section.viewLinks must not be empty
      }

      "when there is multiple" in {

        val answers = emptyUserAnswers
          .setValue(DocumentReferenceNumberPage(hcIndex, Index(0)), "ref1")
          .setValue(AdditionalInformationPage(hcIndex, Index(0)), "additional info")
          .setValue(TypePage(hcIndex, Index(0)), DocumentType(Transport, "code", "description"))
          .setValue(DocumentReferenceNumberPage(hcIndex, Index(1)), "ref2")
          .setValue(AdditionalInformationPage(hcIndex, Index(1)), "additional info2")
          .setValue(TypePage(hcIndex, Index(1)), DocumentType(Support, "code", "description"))

        setExistingUserAnswers(answers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.section.children(1)

        section.sectionTitle.value mustBe "Documents"
        section.children.head.sectionTitle.value mustBe "Document 1"
        section.children.head.rows.size mustBe 3

        section.children(1).sectionTitle.value mustBe "Document 2"
        section.children(1).rows.size mustBe 3

        section.viewLinks must not be empty
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
          .setValue(SecurityIndicatorFromExportDeclarationPage(hcIndex), SecurityType("Code", "Description"))

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new HouseConsignmentViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers, index)

        result.section.rows.size mustBe 1
        result.section.rows.head.value.value mustBe "Code - Description"

        result.section.children.head mustBe a[AccordionSection]
        result.section.children.head.sectionTitle.value mustBe "Departure means of transport"

        result.section.children(1) mustBe a[AccordionSection]
        result.section.children(1).sectionTitle.value mustBe "Documents"
        result.section.children(1).viewLinks must not be empty

        result.section.children(2) mustBe a[AccordionSection]
        result.section.children(2).sectionTitle.value mustBe "Additional information"
        result.section.children(2).viewLinks mustBe empty

        result.section.children(3) mustBe a[AccordionSection]
        result.section.children(3).sectionTitle.value mustBe "Items"
        result.section.children(3).viewLinks must not be empty

        result.section.children(3).children.head mustBe a[AccordionSection]
        result.section.children(3).children.head.sectionTitle.value mustBe "Item 1"
        result.section.children(3).children.head.rows.size mustBe 5

        result.section.children(3).children.head.children.head mustBe a[AccordionSection]
        result.section.children(3).children.head.children.head.sectionTitle.value mustBe "UN numbers"
        result.section.children(3).children.head.children.head.rows.size mustBe 0

        result.section.children(3).children.head.children(1) mustBe a[StaticSection]
        result.section.children(3).children.head.children(1).sectionTitle.value mustBe "Consignee"
        result.section.children(3).children.head.children(1).rows.size mustBe 2

        result.section.children(3).children.head.children(2) mustBe a[AccordionSection]
        result.section.children(3).children.head.children(2).sectionTitle.value mustBe "Documents"
        result.section.children(3).children.head.children(2).viewLinks.head.id mustBe "add-remove-item-1-document"
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
        val section           = result.section.children(3)

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

        result.section.children(3) mustBe a[AccordionSection]
        result.section.children(3).sectionTitle.value mustBe "Items"
        result.section.children(3).viewLinks must not be empty

        result.section.children(3).children.head mustBe a[AccordionSection]
        result.section.children(3).children.head.sectionTitle.value mustBe "Item 1"
        result.section.children(3).children.head.rows.size mustBe 5

        result.section.children(3).children(1) mustBe a[AccordionSection]
        result.section.children(3).children(1).sectionTitle.value mustBe "Item 2"
        result.section.children(3).children(1).rows.size mustBe 5

        result.section.children(3).children.head.children.head mustBe a[AccordionSection]
        result.section.children(3).children.head.children.head.sectionTitle.value mustBe "UN numbers"
        result.section.children(3).children.head.children.head.rows.size mustBe 0

        result.section.children(3).children.head.children(1) mustBe a[StaticSection]
        result.section.children(3).children.head.children(1).sectionTitle.value mustBe "Consignee"
        result.section.children(3).children.head.children(1).rows.size mustBe 2
      }
    }
  }
}
