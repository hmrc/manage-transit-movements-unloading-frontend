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
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.{AdditionalReferenceType, Country, CustomsOffice}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import pages.houseConsignment.index.items.{GrossWeightPage, ItemDescriptionPage}
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import pages._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.UnloadingFindingsViewModel.UnloadingFindingsViewModelProvider

import scala.concurrent.Future

class UnloadingFindingsViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]
  private val countryDesc                            = "Great Britain"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "Unloading findings sections" - {

    val customsOffice = CustomsOffice("id", "name", "countryId", None)

    "must render pre-section" in {
      val viewModelProvider = new UnloadingFindingsViewModelProvider()
      val result            = viewModelProvider.apply(emptyUserAnswers.setValue(CustomsOfficeOfDestinationActualPage, customsOffice))
      val section           = result.sections.head

      section.sectionTitle must not be defined

      section.rows.size mustBe 2
      section.rows.head.key.value mustBe "Office of destination"
      section.rows(1).key.value mustBe "Authorised consignee’s EORI number or Trader Identification Number (TIN)"

      section.viewLink must not be defined
    }

    "must render Means of Transport section" - {
      "when there is one" in {
        val userAnswers = emptyUserAnswers
          .setValue(TransportMeansIdentificationPage(dtmIndex), TransportMeansIdentification("4", ""))
          .setValue(VehicleIdentificationNumberPage(dtmIndex), "28")
          .setValue(CountryPage(dtmIndex), Country("GB", ""))
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

        setExistingUserAnswers(userAnswers)

        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(1)

        section.sectionTitle.value mustBe "Departure means of transport 1"
        section.rows.size mustBe 3
        section.viewLink must not be defined
      }

      "when there is multiple" in {
        val userAnswers = emptyUserAnswers
          .setValue(TransportMeansIdentificationPage(Index(0)), TransportMeansIdentification("4", ""))
          .setValue(VehicleIdentificationNumberPage(Index(0)), "28")
          .setValue(CountryPage(Index(0)), Country("GB", ""))
          .setValue(TransportMeansIdentificationPage(Index(1)), TransportMeansIdentification("4", ""))
          .setValue(VehicleIdentificationNumberPage(Index(1)), "28")
          .setValue(CountryPage(Index(1)), Country("GB", ""))
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(2)

        section.sectionTitle.value mustBe "Departure means of transport 2"
        section.rows.size mustBe 3
        section.viewLink must not be defined
      }
    }

    "must render transport equipment section" - {
      "when there is one" - {

        "with no seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "cin-1")
            .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val viewModelProvider = new UnloadingFindingsViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(1)

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 1
          section.viewLink must not be defined
        }

        "with seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "cin-1")
            .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "1002")
            .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val viewModelProvider = new UnloadingFindingsViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(1)

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 2
          section.viewLink must not be defined
        }
      }
      "when there is multiple" - {

        "with no seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(Index(0)), "cin-1")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "cin-1")
            .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val viewModelProvider = new UnloadingFindingsViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(2)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 1
          section.viewLink must not be defined
        }

        "with seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(Index(0)), "cin-1")
            .setValue(SealIdentificationNumberPage(Index(0), sealIndex), "1002")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "cin-1")
            .setValue(SealIdentificationNumberPage(Index(1), sealIndex), "1002")
            .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

          val viewModelProvider = new UnloadingFindingsViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(2)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 2
          section.viewLink must not be defined
        }
      }
    }

    "must render house consignment sections" - {
      "when there is one" in {
        val userAnswers = emptyUserAnswers
          .setValue(ConsignorNamePage(hcIndex), "michael doe")
          .setValue(ConsignorIdentifierPage(hcIndex), "csgr1")
          .setValue(ConsigneeNamePage(hcIndex), "John Smith")
          .setValue(ConsigneeIdentifierPage(hcIndex), "csgee1")
          .setValue(DepartureTransportMeansIdentificationTypePage(hcIndex, dtmIndex), TransportMeansIdentification("2", ""))
          .setValue(DepartureTransportMeansIdentificationNumberPage(hcIndex, dtmIndex), "23")
          .setValue(DepartureTransportMeansCountryPage(hcIndex, dtmIndex), Country("IT", ""))
          .setValue(ItemDescriptionPage(hcIndex, itemIndex), "shirts")
          .setValue(GrossWeightPage(hcIndex, itemIndex), BigDecimal(123.45))
          .setValue(NetWeightPage(hcIndex, itemIndex), 123.45)
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(1)

        section.sectionTitle.value mustBe "House consignment 1"
        section.rows.size mustBe 4
        section.viewLink mustBe defined
      }
      "when there is multiple" in {
        val userAnswers = emptyUserAnswers
          .setValue(ConsignorNamePage(Index(0)), "michael doe")
          .setValue(ConsignorIdentifierPage(Index(0)), "csgr1")
          .setValue(ConsigneeNamePage(Index(0)), "John Smith")
          .setValue(ConsigneeIdentifierPage(Index(0)), "csgee1")
          .setValue(DepartureTransportMeansIdentificationTypePage(Index(0), dtmIndex), TransportMeansIdentification("2", ""))
          .setValue(DepartureTransportMeansIdentificationNumberPage(Index(0), dtmIndex), "23")
          .setValue(DepartureTransportMeansCountryPage(Index(0), dtmIndex), Country("IT", ""))
          .setValue(ItemDescriptionPage(Index(0), itemIndex), "shirts")
          .setValue(GrossWeightPage(Index(0), itemIndex), BigDecimal(123.45))
          .setValue(NetWeightPage(Index(0), itemIndex), 123.45)
          .setValue(ConsignorNamePage(Index(1)), "michael doe")
          .setValue(ConsignorIdentifierPage(Index(1)), "csgr1")
          .setValue(ConsigneeNamePage(Index(1)), "John Smith")
          .setValue(ConsigneeIdentifierPage(Index(1)), "csgee1")
          .setValue(DepartureTransportMeansIdentificationTypePage(Index(1), dtmIndex), TransportMeansIdentification("2", ""))
          .setValue(DepartureTransportMeansIdentificationNumberPage(Index(1), dtmIndex), "23")
          .setValue(DepartureTransportMeansCountryPage(Index(1), dtmIndex), Country("IT", ""))
          .setValue(ItemDescriptionPage(Index(1), itemIndex), "shirts")
          .setValue(GrossWeightPage(Index(1), itemIndex), BigDecimal(123.45))
          .setValue(NetWeightPage(Index(1), itemIndex), 123.45)
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(2)

        section.sectionTitle.value mustBe "House consignment 2"
        section.rows.size mustBe 4
        section.viewLink mustBe defined
      }
    }

    "must render additional references sections" - {
      "when there is one" in {

        val userAnswers = emptyUserAnswers
          .setValue(AdditionalReferenceTypePage(index), AdditionalReferenceType("Y015", "The rough diamonds are contained in ..."))
          .setValue(AdditionalReferenceNumberPage(index), "addref-1")
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(1)

        section.sectionTitle.value mustBe "Additional references"
        section.rows.size mustBe 1
      }

      "when there are multiple" in {

        val userAnswers = emptyUserAnswers
          .setValue(AdditionalReferenceTypePage(Index(0)), AdditionalReferenceType("Y015", "The rough diamonds are contained in ..."))
          .setValue(AdditionalReferenceNumberPage(Index(0)), "addref-1")
          .setValue(AdditionalReferenceTypePage(Index(1)), AdditionalReferenceType("Y022", "Consignor / exporter (AEO certificate number)"))
          .setValue(AdditionalReferenceNumberPage(Index(1)), "addref-2")
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(1)

        section.sectionTitle.value mustBe "Additional references"
        section.rows.size mustBe 2
      }
    }
  }
}
