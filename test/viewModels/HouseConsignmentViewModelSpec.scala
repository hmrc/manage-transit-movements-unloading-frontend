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
import models.reference.*
import models.{CheckMode, Index}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.*
import pages.houseConsignment.index.additionalReference.{HouseConsignmentAdditionalReferenceNumberPage, HouseConsignmentAdditionalReferenceTypePage}
import pages.houseConsignment.index.additionalinformation.{HouseConsignmentAdditionalInformationCodePage, HouseConsignmentAdditionalInformationTextPage}
import pages.houseConsignment.index.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import pages.houseConsignment.index.documents.{AdditionalInformationPage, DocumentReferenceNumberPage, TypePage}
import pages.houseConsignment.index.items.{
  ConsigneeIdentifierPage as ItemConsigneeIdentifierPage,
  ConsigneeNamePage as ItemConsigneeNamePage,
  GrossWeightPage as ItemGrossWeightPage,
  ItemDescriptionPage,
  NetWeightPage
}
import pages.houseConsignment.index.{
  CountryOfDestinationPage,
  GrossWeightPage as HouseConsignmentGrossWeightPage,
  SecurityIndicatorFromExportDeclarationPage,
  UniqueConsignmentReferencePage as HouseConsignmentUniqueConsignmentReferencePage
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
          .setValue(VehicleIdentificationNumberPage(Index(0), index), vehicleIdentificationNumber)
          .setValue(TransportMeansIdentificationPage(Index(0), index), identificationType)
          .setValue(CountryPage(Index(0), index), country)

        setExistingUserAnswers(answers)

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.section.children(2)

        section.sectionTitle.value mustEqual "Departure means of transport"
        section.children.size mustEqual 1

        section.children.head.sectionTitle.value mustEqual "Departure means of transport 1"
        section.children.head.rows.size mustEqual 3

        section.viewLinks.head.href mustEqual controllers.houseConsignment.index.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController
          .onPageLoad(arrivalId, houseConsignmentIndex, CheckMode)
          .url
      }

      "when there is multiple" in {

        val answers = emptyUserAnswers
          .setValue(VehicleIdentificationNumberPage(hcIndex, Index(0)), vehicleIdentificationNumber)
          .setValue(TransportMeansIdentificationPage(hcIndex, Index(0)), identificationType)
          .setValue(CountryPage(hcIndex, Index(0)), country)
          .setValue(VehicleIdentificationNumberPage(hcIndex, Index(1)), vehicleIdentificationNumber)
          .setValue(TransportMeansIdentificationPage(hcIndex, Index(1)), identificationType)
          .setValue(CountryPage(hcIndex, Index(1)), country)

        setExistingUserAnswers(answers)

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.section.children(2)

        section.sectionTitle.value mustEqual "Departure means of transport"
        section.children.size mustEqual 2

        section.children.head.sectionTitle.value mustEqual "Departure means of transport 1"
        section.children.head.rows.size mustEqual 3

        section.children(1).sectionTitle.value mustEqual "Departure means of transport 2"
        section.children(1).rows.size mustEqual 3

        section.viewLinks.head.href mustEqual controllers.houseConsignment.index.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController
          .onPageLoad(arrivalId, houseConsignmentIndex, CheckMode)
          .url
      }
    }

    "must render Documents section" - {
      "when there is one" in {

        val answers = emptyUserAnswers
          .setValue(DocumentReferenceNumberPage(houseConsignmentIndex, documentIndex), "ref1")
          .setValue(AdditionalInformationPage(houseConsignmentIndex, documentIndex), "additional info")
          .setValue(TypePage(houseConsignmentIndex, documentIndex), DocumentType(Transport, "code", "description"))

        setExistingUserAnswers(answers)

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.section.children(3)

        section.sectionTitle.value mustEqual "Documents"
        section.children.head.sectionTitle.value mustEqual "Document 1"
        section.children.head.rows.size mustEqual 3

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

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.section.children(3)

        section.sectionTitle.value mustEqual "Documents"
        section.children.head.sectionTitle.value mustEqual "Document 1"
        section.children.head.rows.size mustEqual 3

        section.children(1).sectionTitle.value mustEqual "Document 2"
        section.children(1).rows.size mustEqual 3

        section.viewLinks must not be empty
      }
    }

    "must render Additional Reference section" - {
      val referenceType = arbitrary[AdditionalReferenceType].sample.value
      val number        = nonEmptyString.sample.value
      "when there is one" in {

        val answers = emptyUserAnswers
          .setValue(HouseConsignmentAdditionalReferenceTypePage(hcIndex, Index(0)), referenceType)
          .setValue(HouseConsignmentAdditionalReferenceNumberPage(hcIndex, Index(0)), number)

        setExistingUserAnswers(answers)

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.section.children(4)

        section.sectionTitle.value mustEqual "Additional references"
        section.children.size mustEqual 1

        section.children.head.sectionTitle.value mustEqual "Additional reference 1"
        section.children.head.rows.size mustEqual 2

        section.viewLinks.head.href mustEqual controllers.houseConsignment.index.additionalReference.routes.AddAnotherAdditionalReferenceController
          .onSubmit(arrivalId, CheckMode, hcIndex)
          .url
      }

      "when there is multiple" in {

        val answers = emptyUserAnswers
          .setValue(HouseConsignmentAdditionalReferenceTypePage(hcIndex, Index(0)), referenceType)
          .setValue(HouseConsignmentAdditionalReferenceNumberPage(hcIndex, Index(0)), number)
          .setValue(HouseConsignmentAdditionalReferenceTypePage(hcIndex, Index(1)), referenceType)
          .setValue(HouseConsignmentAdditionalReferenceNumberPage(hcIndex, Index(1)), number)

        setExistingUserAnswers(answers)

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.section.children(4)

        section.sectionTitle.value mustEqual "Additional references"
        section.children.size mustEqual 2

        section.children.head.sectionTitle.value mustEqual "Additional reference 1"
        section.children.head.rows.size mustEqual 2

        section.children(1).sectionTitle.value mustEqual "Additional reference 2"
        section.children(1).rows.size mustEqual 2

        section.viewLinks.head.href mustEqual controllers.houseConsignment.index.additionalReference.routes.AddAnotherAdditionalReferenceController
          .onSubmit(arrivalId, CheckMode, hcIndex)
          .url
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

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.section.children(5)

        section.sectionTitle.value mustEqual "Additional information"
        section.children.size mustEqual 1

        section.children.head.sectionTitle.value mustEqual "Additional information 1"
        section.children.head.rows.size mustEqual 2

        section.viewLinks mustEqual Nil
      }

      "when there is multiple" in {

        val answers = emptyUserAnswers
          .setValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, Index(0)), code)
          .setValue(HouseConsignmentAdditionalInformationTextPage(hcIndex, Index(0)), description)
          .setValue(HouseConsignmentAdditionalInformationCodePage(hcIndex, Index(1)), code)
          .setValue(HouseConsignmentAdditionalInformationTextPage(hcIndex, Index(1)), description)

        setExistingUserAnswers(answers)

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(answers, index)
        val section           = result.section.children(5)

        section.sectionTitle.value mustEqual "Additional information"
        section.children.size mustEqual 2

        section.children.head.sectionTitle.value mustEqual "Additional information 1"
        section.children.head.rows.size mustEqual 2

        section.children(1).sectionTitle.value mustEqual "Additional information 2"
        section.children(1).rows.size mustEqual 2

        section.viewLinks mustEqual Nil
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
          .setValue(ItemGrossWeightPage(hcIndex, itemIndex), BigDecimal(123.45))
          .setValue(NetWeightPage(hcIndex, itemIndex), BigDecimal(123.45))
          .setValue(ItemConsigneeNamePage(hcIndex, itemIndex), "John Smith")
          .setValue(ItemConsigneeIdentifierPage(hcIndex, itemIndex), "csgee2")
          .setValue(SecurityIndicatorFromExportDeclarationPage(hcIndex), SecurityType("Code", "Description"))
          .setValue(CountryOfDestinationPage(hcIndex), Country("FR", "France"))
          .setValue(HouseConsignmentGrossWeightPage(hcIndex), BigDecimal(123.45))
          .setValue(HouseConsignmentUniqueConsignmentReferencePage(hcIndex), "ucr")

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers, index)

        result.section.rows.size mustEqual 4
        result.section.rows(0).value.value mustEqual "France"
        result.section.rows(1).value.value mustEqual "Description"
        result.section.rows(2).value.value mustEqual "123.45kg"
        result.section.rows(3).value.value mustEqual "ucr"

        result.section.children(6) mustBe a[AccordionSection]
        result.section.children(6).sectionTitle.value mustEqual "Items"
        result.section.children(6).viewLinks must not be empty

        result.section.children(6).children.head mustBe a[AccordionSection]
        result.section.children(6).children.head.sectionTitle.value mustEqual "Item 1"
        result.section.children(6).children.head.rows.size mustEqual 5

        result.section.children(6).children.head.children.head mustBe a[AccordionSection]
        result.section.children(6).children.head.children.head.sectionTitle.value mustEqual "UN numbers"
        result.section.children(6).children.head.children.head.rows.size mustEqual 0

        result.section.children(6).children.head.children(1) mustBe a[StaticSection]
        result.section.children(6).children.head.children(1).sectionTitle.value mustEqual "Consignee"
        result.section.children(6).children.head.children(1).rows.size mustEqual 2

        result.section.children(6).children.head.children(2) mustBe a[AccordionSection]
        result.section.children(6).children.head.children(2).sectionTitle.value mustEqual "Documents"
        result.section.children(6).children.head.children(2).viewLinks.head.id mustEqual "add-remove-item-1-document"
      }
    }

    "must render item section" - {
      "when there is one" in {
        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(hcIndex, itemIndex), "shirts")
          .setValue(ItemGrossWeightPage(hcIndex, itemIndex), BigDecimal(123.45))
          .setValue(NetWeightPage(hcIndex, itemIndex), BigDecimal(123.45))

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers, index)
        val section           = result.section.children(6)

        section.sectionTitle.value mustEqual "Items"
        section.children.head.sectionTitle.value mustEqual "Item 1"
        section.children.head.rows.size mustEqual 5
      }

      "when there are multiple" in {
        val userAnswers = emptyUserAnswers
          .setValue(ConsignorNamePage(hcIndex), "michael doe")
          .setValue(ConsignorIdentifierPage(hcIndex), "csgr1")
          .setValue(ConsigneeNamePage(hcIndex), "John Smith")
          .setValue(ConsigneeIdentifierPage(hcIndex), "csgee1")
          .setValue(ItemDescriptionPage(hcIndex, Index(0)), "shirts")
          .setValue(ItemGrossWeightPage(hcIndex, Index(0)), BigDecimal(123.45))
          .setValue(NetWeightPage(hcIndex, Index(0)), BigDecimal(123.45))
          .setValue(ItemDescriptionPage(hcIndex, Index(1)), "shirts")
          .setValue(ItemGrossWeightPage(hcIndex, Index(1)), BigDecimal(123.45))
          .setValue(NetWeightPage(hcIndex, Index(1)), BigDecimal(123.45))
          .setValue(ItemConsigneeNamePage(hcIndex, Index(0)), "John Smith")
          .setValue(ItemConsigneeIdentifierPage(hcIndex, Index(0)), "csgee2")
          .setValue(ItemConsigneeNamePage(hcIndex, Index(1)), "John Smith")
          .setValue(ItemConsigneeIdentifierPage(hcIndex, Index(1)), "csgee3")

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = app.injector.instanceOf[HouseConsignmentViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers, index)

        result.section.children(6) mustBe a[AccordionSection]
        result.section.children(6).sectionTitle.value mustEqual "Items"
        result.section.children(6).viewLinks must not be empty

        result.section.children(6).children.head mustBe a[AccordionSection]
        result.section.children(6).children.head.sectionTitle.value mustEqual "Item 1"
        result.section.children(6).children.head.rows.size mustEqual 5

        result.section.children(6).children(1) mustBe a[AccordionSection]
        result.section.children(6).children(1).sectionTitle.value mustEqual "Item 2"
        result.section.children(6).children(1).rows.size mustEqual 5

        result.section.children(6).children.head.children.head mustBe a[AccordionSection]
        result.section.children(6).children.head.children.head.sectionTitle.value mustEqual "UN numbers"
        result.section.children(6).children.head.children.head.rows.size mustEqual 0

        result.section.children(6).children.head.children(1) mustBe a[StaticSection]
        result.section.children(6).children.head.children(1).sectionTitle.value mustEqual "Consignee"
        result.section.children(6).children.head.children(1).rows.size mustEqual 2
      }
    }
  }
}
