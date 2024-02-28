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
import generated.{AddressType10, HolderOfTheTransitProcedureType06, Number0}
import generators.Generators
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.{AdditionalReferenceType, Country, CustomsOffice, Incident}
import models.{Index, SecurityType, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import pages.holderOfTheTransitProcedure.{CountryPage => HotPCountryPage}
import pages.houseConsignment.index.items.{GrossWeightPage, ItemDescriptionPage}
import pages.incident.{IncidentCodePage, IncidentTextPage}
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import scalaxb.XMLCalendar
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

    "must render header section" in {
      val viewModelProvider = new UnloadingFindingsViewModelProvider()
      val userAnswers: UserAnswers = emptyUserAnswers.copy(ie043Data =
        emptyUserAnswers.ie043Data.copy(TransitOperation =
          emptyUserAnswers.ie043Data.TransitOperation.copy(
            reducedDatasetIndicator = Number0,
            declarationType = Some("T1"),
            declarationAcceptanceDate = Some(XMLCalendar("2020-01-01T09:30:00"))
          )
        )
      )

      val result = viewModelProvider.apply(
        userAnswers
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)
          .setValue(SecurityTypePage, SecurityType("1", "test"))
      )
      val section = result.sections.head

      section.sectionTitle must not be defined
      section.rows.size mustBe 6
      section.viewLinks mustBe Nil
    }

    "must render Holder of the Transit Procedure section" - {
      "when there is one" in {
        val country = Country("GB", "Great Britain")
        val userAnswers = emptyUserAnswers
          .copy(ie043Data =
            basicIe043.copy(HolderOfTheTransitProcedure =
              Some(
                HolderOfTheTransitProcedureType06(
                  Some("identificationNumber"),
                  Some("TIRHolderIdentificationNumber"),
                  "name",
                  AddressType10("streetAndNumber", Some("postcode"), "city", "GB")
                )
              )
            )
          )
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)
          .setValue(HotPCountryPage, country)

        setExistingUserAnswers(userAnswers)

        when(mockReferenceDataService.getCountryByCode(any())(any(), any())).thenReturn(Future.successful(country))

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(1)

        section.sectionTitle.value mustBe "Transit holder"
        section.rows.size mustBe 5
        section.viewLinks mustBe Nil
      }

      "when there is none" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = None))
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(1)

        section.sectionTitle.value must not be "Transit holder"
      }
    }

    "must render Departure Means of Transport section" in {

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
      section.viewLinks must not be Nil
      section.accordionLink must not be defined
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
      setExistingUserAnswers(userAnswers)

      when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

      val viewModelProvider = new UnloadingFindingsViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)
      val section           = result.sections(1)
      val section2          = result.sections(2)

      section.sectionTitle.value mustBe "Departure means of transport 1"
      section.rows.size mustBe 3
      section.viewLinks mustBe Nil
      section.accordionLink must not be defined

      section2.sectionTitle.value mustBe "Departure means of transport 2"
      section2.rows.size mustBe 3
      section2.viewLinks must not be Nil
      section2.accordionLink must not be defined
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
          val section           = result.sections(2)

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 1
          section.viewLinks must not be Nil
          section.accordionLink must not be defined
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
          val section           = result.sections(2)

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 2
          section.viewLinks must not be Nil
          section.accordionLink must not be defined
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
          val section           = result.sections(3)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 1
          section.viewLinks must not be Nil
          section.accordionLink must not be defined
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
          val section           = result.sections(3)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 2
          section.viewLinks must not be Nil
          section.accordionLink must not be defined
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
        val section           = result.sections(5)

        section.sectionTitle.value mustBe "House consignment 1"
        section.rows.size mustBe 4
        section.viewLinks mustBe Nil
        section.accordionLink mustBe defined
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
        val section           = result.sections(6)

        section.sectionTitle.value mustBe "House consignment 2"
        section.rows.size mustBe 4
        section.viewLinks mustBe Nil
        section.accordionLink mustBe defined
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
        val section           = result.sections(4)

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
        val section           = result.sections(4)

        section.sectionTitle.value mustBe "Additional references"
        section.rows.size mustBe 2
        section.viewLinks must not be Nil
        section.accordionLink must not be defined
      }
    }

    "must render incidents sections" - {
      "when there is one" in {

        val userAnswers = emptyUserAnswers
          .setValue(IncidentCodePage(index), Incident("1", "bad wrapping paper"))
          .setValue(IncidentTextPage(index), "it got wet")
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(5)

        section.sectionTitle.value mustBe "Incident 1"
        section.rows.size mustBe 2
        section.viewLinks mustBe Nil
        section.accordionLink must not be defined
      }

      "when there are multiple" in {

        val userAnswers = emptyUserAnswers
          .setValue(IncidentCodePage(Index(0)), Incident("1", "desc 1"))
          .setValue(IncidentTextPage(Index(0)), "free text 1")
          .setValue(IncidentCodePage(Index(1)), Incident("2", "desc 2"))
          .setValue(IncidentTextPage(Index(1)), "free text 2")
          .setValue(CustomsOfficeOfDestinationActualPage, customsOffice)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)

        val section1 = result.sections(5)
        section1.sectionTitle.value mustBe "Incident 1"
        section1.rows.size mustBe 2
        section1.viewLinks mustBe Nil
        section1.accordionLink must not be defined

        val section2 = result.sections(6)
        section2.sectionTitle.value mustBe "Incident 2"
        section2.rows.size mustBe 2
        section1.viewLinks mustBe Nil
        section1.accordionLink must not be defined
      }
    }
  }
}
