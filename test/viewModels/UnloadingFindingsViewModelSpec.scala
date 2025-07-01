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
import generated.*
import generators.Generators
import models.reference.*
import models.{Index, SecurityType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.*
import pages.additionalInformation.{AdditionalInformationCodePage, AdditionalInformationTextPage}
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.countriesOfRouting.CountryOfRoutingPage
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import pages.holderOfTheTransitProcedure.CountryPage as HotPCountryPage
import pages.houseConsignment.index.items.{GrossWeightPage, ItemDescriptionPage, NetWeightPage}
import pages.incident.{IncidentCodePage, IncidentTextPage}
import pages.transportEquipment.index.ItemPage
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.test.Helpers.running
import scalaxb.XMLCalendar
import viewModels.UnloadingFindingsViewModel.UnloadingFindingsViewModelProvider

class UnloadingFindingsViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "Unloading findings sections" - {

    "must render header section" in {
      val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
      val userAnswers: UserAnswers = emptyUserAnswers.copy(ie043Data =
        emptyUserAnswers.ie043Data.copy(TransitOperation =
          emptyUserAnswers.ie043Data.TransitOperation.copy(
            reducedDatasetIndicator = Number0,
            declarationType = Some("T1"),
            declarationAcceptanceDate = Some(XMLCalendar("2020-01-01T09:30:00"))
          )
        )
      )

      val customsOffice = CustomsOffice("id", "name", "countryId", None)

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

    "consignor section" - {
      "must not be rendered" - {
        "when there is not a consignor" in {

          val ie043 = arbitrary[CC043CType].retryUntil(_.Consignment.flatMap(_.Consignor).isEmpty).sample.value

          val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(1)

          section.sectionTitle must not be "Consignor"
        }
      }

      "must be rendered" - {
        "when there is a consignor" in {
          forAll(arbitrary[CC043CType].retryUntil(_.Consignment.flatMap(_.Consignor).isDefined)) {
            ie043 =>
              val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

              val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
              val result            = viewModelProvider.apply(userAnswers)
              val section           = result.sections(1)

              section.sectionTitle.value mustBe "Consignor"
              section.viewLinks mustBe Nil
          }
        }
      }
    }

    "consignee section" - {
      "must not be rendered" - {
        "when there is not a consignee" in {

          val ie043 = arbitrary[CC043CType].retryUntil(_.Consignment.flatMap(_.Consignee).isEmpty).sample.value

          val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(1)

          section.sectionTitle must not be "Consignee"
        }
      }

      "must be rendered" - {
        "when there is a consignee" in {
          forAll(arbitrary[CC043CType].retryUntil(_.Consignment.flatMap(_.Consignee).isDefined)) {
            ie043 =>
              val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

              val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
              val result            = viewModelProvider.apply(userAnswers)
              val section           = result.sections.find(_.sectionTitle.contains("Consignee")).get

              section.viewLinks mustBe Nil
          }
        }
      }
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
                  AddressType15("streetAndNumber", Some("postcode"), "city", "GB")
                )
              )
            )
          )
          .setValue(HotPCountryPage, country)

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(1)

        section.sectionTitle.value mustBe "Transit holder"
        section.rows.size mustBe 5
        section.viewLinks mustBe Nil
      }

      "when there is none" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = None))

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(1)

        section.sectionTitle.value must not be "Transit holder"
      }
    }

    "must render inland mode of transport section" - {
      "when there is one" in {

        val userAnswers = emptyUserAnswers
          .copy(ie043Data =
            basicIe043.copy(Consignment =
              Some(
                ConsignmentType05(
                  containerIndicator = arbitraryFlag.arbitrary.sample.get,
                  inlandModeOfTransport = Some("Mode")
                )
              )
            )
          )

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(1)

        section.sectionTitle.value mustBe "Inland mode of transport"
        section.viewLinks mustBe Nil

      }

      "when there is none" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data =
            basicIe043.copy(Consignment =
              Some(
                ConsignmentType05(
                  containerIndicator = arbitraryFlag.arbitrary.sample.get,
                  inlandModeOfTransport = None
                )
              )
            )
          )

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(1)

        section.sectionTitle must not be "Inland mode Of transport"
      }
    }

    "must render Countries of routing section" - {
      "when phase 6 is enabled" in {
        val app = super
          .guiceApplicationBuilder()
          .configure("feature-flags.phase-6-enabled" -> true)
          .build()

        running(app) {
          val userAnswers = emptyUserAnswers
            .setValue(CountryOfRoutingPage(index), Country("GB", "United Kingdom"))

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(2)

          section.sectionTitle.value mustBe "Countries of routing"
          section.viewLinks must not be Nil
          section.children.length mustBe 0
          section.rows.length mustBe 1
        }
      }
    }

    "must not render Countries of routing section" - {
      "when phase 6 is not enabled" in {
        val app = super
          .guiceApplicationBuilder()
          .configure("feature-flags.phase-6-enabled" -> false)
          .build()

        running(app) {
          val userAnswers = emptyUserAnswers
            .setValue(CountryOfRoutingPage(index), Country("GB", "United Kingdom"))

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)

          result.sections.filter(_.sectionTitle.contains("Countries of routing")) must be(empty)
        }
      }
    }

    "must render Departure Means of Transport section" in {

      val userAnswers = emptyUserAnswers
        .setValue(TransportMeansIdentificationPage(dtmIndex), TransportMeansIdentification("4", ""))
        .setValue(VehicleIdentificationNumberPage(dtmIndex), "28")
        .setValue(CountryPage(dtmIndex), Country("GB", ""))

      val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
      val result            = viewModelProvider.apply(userAnswers)
      val section           = result.sections(2)

      section.sectionTitle.value mustBe "Departure means of transport"
      section.viewLinks must not be Nil
      section.children.length mustBe 1

      section.children.head.sectionTitle.value mustBe "Departure means of transport 1"
      section.children.head.rows.size mustBe 3
      section.children.head.viewLinks mustBe Nil
    }

    "when there is multiple" in {
      val userAnswers = emptyUserAnswers
        .setValue(TransportMeansIdentificationPage(Index(0)), TransportMeansIdentification("4", ""))
        .setValue(VehicleIdentificationNumberPage(Index(0)), "28")
        .setValue(CountryPage(Index(0)), Country("GB", ""))
        .setValue(TransportMeansIdentificationPage(Index(1)), TransportMeansIdentification("4", ""))
        .setValue(VehicleIdentificationNumberPage(Index(1)), "28")
        .setValue(CountryPage(Index(1)), Country("GB", ""))

      val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
      val result            = viewModelProvider.apply(userAnswers)
      val section           = result.sections(2)

      section.sectionTitle.value mustBe "Departure means of transport"
      section.children.length mustBe 2
      section.viewLinks must not be Nil

      section.children.head.sectionTitle.value mustBe "Departure means of transport 1"
      section.children.head.rows.size mustBe 3
      section.children.head.viewLinks mustBe Nil

      section.children(1).sectionTitle.value mustBe "Departure means of transport 2"
      section.children(1).rows.size mustBe 3
      section.children(1).viewLinks mustBe Nil
    }

    "must render transport equipment section" - {
      "when there is one" - {

        "with no seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "cin-1")

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(3)

          section.sectionTitle.value mustBe "Transport equipment"
          section.viewLinks must not be Nil
          section.children.length mustBe 1

          section.children.head.sectionTitle.value mustBe "Transport equipment 1"
          section.children.head.rows.size mustBe 1
          section.children.head.viewLinks mustBe Nil
          section.children.head.children.size mustBe 2
        }

        "with seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "cin-1")
            .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "1002")

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(3)

          section.sectionTitle.value mustBe "Transport equipment"
          section.viewLinks must not be Nil
          section.children.length mustBe 1

          section.children.head.sectionTitle.value mustBe "Transport equipment 1"
          section.children.head.rows.size mustBe 1
          section.children.head.viewLinks mustBe Nil
          section.children.head.children.size mustBe 2

          val seals = section.children.head.children.head
          seals.sectionTitle.value mustBe "Seals"
          seals.rows.size mustBe 1
          seals.viewLinks must not be Nil
        }
      }
      "when there is multiple" - {

        "with no seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(Index(0)), "cin-1")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "cin-1")

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(3)

          section.sectionTitle.value mustBe "Transport equipment"
          section.viewLinks must not be Nil
          section.children.length mustBe 2

          section.children.head.sectionTitle.value mustBe "Transport equipment 1"
          section.children.head.rows.size mustBe 1
          section.children.head.viewLinks mustBe Nil
          section.children.head.children.size mustBe 2

          section.children(1).sectionTitle.value mustBe "Transport equipment 2"
          section.children(1).rows.size mustBe 1
          section.children(1).viewLinks mustBe Nil
          section.children.head.children.size mustBe 2
        }

        "with seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(Index(0)), "cin-1")
            .setValue(SealIdentificationNumberPage(Index(0), sealIndex), "1002")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "cin-1")
            .setValue(SealIdentificationNumberPage(Index(1), sealIndex), "1002")

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(3)

          section.sectionTitle.value mustBe "Transport equipment"
          section.children.length mustBe 2

          section.children.head.sectionTitle.value mustBe "Transport equipment 1"
          section.children.head.rows.size mustBe 1
          section.children.head.viewLinks mustBe Nil
          section.children.head.children.size mustBe 2

          val transportEquipment1Seals = section.children.head.children.head
          transportEquipment1Seals.sectionTitle.value mustBe "Seals"
          transportEquipment1Seals.rows.size mustBe 1
          transportEquipment1Seals.viewLinks must not be Nil

          section.children(1).sectionTitle.value mustBe "Transport equipment 2"
          section.children(1).rows.size mustBe 1
          section.children(1).viewLinks mustBe Nil
          section.children(1).children.size mustBe 2

          val transportEquipment2Seals = section.children(1).children.head
          transportEquipment2Seals.sectionTitle.value mustBe "Seals"
          transportEquipment2Seals.rows.size mustBe 1
          transportEquipment2Seals.viewLinks must not be Nil
        }
      }

      "when there is one" - {

        "with no items" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "cin-1")
            .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "1002")

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(3)

          section.sectionTitle.value mustBe "Transport equipment"
          section.viewLinks must not be Nil
          section.children.length mustBe 1

          section.children.head.sectionTitle.value mustBe "Transport equipment 1"
          section.children.head.rows.size mustBe 1
          section.children.head.viewLinks mustBe Nil
          section.children.head.children.size mustBe 2

          val seals = section.children.head.children.head
          seals.sectionTitle.value mustBe "Seals"
          seals.rows.size mustBe 1
          seals.viewLinks must not be Nil
        }

        "with items" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "cin-1")
            .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "1002")
            .setValue(ItemPage(equipmentIndex, itemIndex), BigInt(10))

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(3)

          section.sectionTitle.value mustBe "Transport equipment"
          section.viewLinks must not be Nil
          section.children.length mustBe 1

          section.children.head.sectionTitle.value mustBe "Transport equipment 1"
          section.children.head.rows.size mustBe 1
          section.children.head.viewLinks mustBe Nil

          section.children.head.children.size mustBe 2

          val seals = section.children.head.children.head
          seals.sectionTitle.value mustBe "Seals"
          seals.rows.size mustBe 1
          seals.viewLinks must not be Nil

          val goodsReferences = section.children.head.children(1)
          goodsReferences.sectionTitle.value mustBe "Items applied to this transport equipment"
          goodsReferences.rows.size mustBe 1
          goodsReferences.viewLinks must not be Nil
        }
      }
      "when there is multiple" - {

        "with no items" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(Index(0)), "cin-1")
            .setValue(SealIdentificationNumberPage(Index(0), sealIndex), "1002")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "cin-1")
            .setValue(SealIdentificationNumberPage(Index(1), sealIndex), "1002")

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(3)

          section.sectionTitle.value mustBe "Transport equipment"
          section.children.length mustBe 2

          section.children.head.sectionTitle.value mustBe "Transport equipment 1"
          section.children.head.rows.size mustBe 1
          section.children.head.viewLinks mustBe Nil
          section.children.head.children.size mustBe 2

          val transportEquipment1Seals = section.children.head.children.head
          transportEquipment1Seals.sectionTitle.value mustBe "Seals"
          transportEquipment1Seals.rows.size mustBe 1
          transportEquipment1Seals.viewLinks must not be Nil

          section.children(1).sectionTitle.value mustBe "Transport equipment 2"
          section.children(1).rows.size mustBe 1
          section.children(1).viewLinks mustBe Nil
          section.children(1).children.size mustBe 2

          val transportEquipment2Seals = section.children(1).children.head
          transportEquipment2Seals.sectionTitle.value mustBe "Seals"
          transportEquipment2Seals.rows.size mustBe 1
          transportEquipment2Seals.viewLinks must not be Nil
        }

        "with items" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(Index(0)), "cin-1")
            .setValue(SealIdentificationNumberPage(Index(0), Index(0)), "1002")
            .setValue(ItemPage(Index(0), Index(0)), BigInt(10))
            .setValue(ContainerIdentificationNumberPage(Index(1)), "cin-1")
            .setValue(SealIdentificationNumberPage(Index(1), Index(0)), "1002")
            .setValue(ItemPage(Index(1), Index(0)), BigInt(10))

          val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.sections(3)

          section.sectionTitle.value mustBe "Transport equipment"
          section.children.length mustBe 2

          section.children.head.sectionTitle.value mustBe "Transport equipment 1"
          section.children.head.rows.size mustBe 1
          section.children.head.viewLinks mustBe Nil
          section.children.head.children.size mustBe 2

          val transportEquipment1Seals = section.children.head.children.head
          transportEquipment1Seals.sectionTitle.value mustBe "Seals"
          transportEquipment1Seals.rows.size mustBe 1
          transportEquipment1Seals.viewLinks must not be Nil

          val transportEquipment1GoodsReferences = section.children.head.children(1)
          transportEquipment1GoodsReferences.sectionTitle.value mustBe "Items applied to this transport equipment"
          transportEquipment1GoodsReferences.rows.size mustBe 1
          transportEquipment1GoodsReferences.viewLinks must not be Nil

          section.children(1).sectionTitle.value mustBe "Transport equipment 2"
          section.children(1).rows.size mustBe 1
          section.children(1).viewLinks mustBe Nil
          section.children.head.children.size mustBe 2

          val transportEquipment2Seals = section.children(1).children.head
          transportEquipment2Seals.sectionTitle.value mustBe "Seals"
          transportEquipment2Seals.rows.size mustBe 1
          transportEquipment2Seals.viewLinks must not be Nil

          val transportEquipment2GoodsReferences = section.children(1).children(1)
          transportEquipment2GoodsReferences.sectionTitle.value mustBe "Items applied to this transport equipment"
          transportEquipment2GoodsReferences.rows.size mustBe 1
          transportEquipment2GoodsReferences.viewLinks must not be Nil
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
          .setValue(pages.houseConsignment.index.departureMeansOfTransport.TransportMeansIdentificationPage(hcIndex, dtmIndex),
                    TransportMeansIdentification("2", "")
          )
          .setValue(pages.houseConsignment.index.departureMeansOfTransport.VehicleIdentificationNumberPage(hcIndex, dtmIndex), "23")
          .setValue(pages.houseConsignment.index.departureMeansOfTransport.CountryPage(hcIndex, dtmIndex), Country("IT", ""))
          .setValue(ItemDescriptionPage(hcIndex, itemIndex), "shirts")
          .setValue(GrossWeightPage(hcIndex, itemIndex), BigDecimal(123.45))
          .setValue(NetWeightPage(hcIndex, itemIndex), BigDecimal(123.45))

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(8)

        section.sectionTitle.value mustBe "House consignments"
        section.children.length mustBe 1

        section.children.head.sectionTitle.value mustBe "House consignment 1"
        section.children.head.rows.size mustBe 2
        section.children.head.viewLinks.size mustBe 1
      }
      "when there is multiple" in {
        val userAnswers = emptyUserAnswers
          .setValue(ConsignorNamePage(Index(0)), "michael doe")
          .setValue(ConsignorIdentifierPage(Index(0)), "csgr1")
          .setValue(ConsigneeNamePage(Index(0)), "John Smith")
          .setValue(ConsigneeIdentifierPage(Index(0)), "csgee1")
          .setValue(pages.houseConsignment.index.departureMeansOfTransport.TransportMeansIdentificationPage(Index(0), dtmIndex),
                    TransportMeansIdentification("2", "")
          )
          .setValue(pages.houseConsignment.index.departureMeansOfTransport.VehicleIdentificationNumberPage(Index(0), dtmIndex), "23")
          .setValue(pages.houseConsignment.index.departureMeansOfTransport.CountryPage(Index(0), dtmIndex), Country("IT", ""))
          .setValue(ItemDescriptionPage(Index(0), itemIndex), "shirts")
          .setValue(GrossWeightPage(Index(0), itemIndex), BigDecimal(123.45))
          .setValue(NetWeightPage(Index(0), itemIndex), BigDecimal(123.45))
          .setValue(ConsignorNamePage(Index(1)), "michael doe")
          .setValue(ConsignorIdentifierPage(Index(1)), "csgr1")
          .setValue(ConsigneeNamePage(Index(1)), "John Smith")
          .setValue(ConsigneeIdentifierPage(Index(1)), "csgee1")
          .setValue(pages.houseConsignment.index.departureMeansOfTransport.TransportMeansIdentificationPage(Index(1), dtmIndex),
                    TransportMeansIdentification("2", "")
          )
          .setValue(pages.houseConsignment.index.departureMeansOfTransport.VehicleIdentificationNumberPage(Index(1), dtmIndex), "23")
          .setValue(pages.houseConsignment.index.departureMeansOfTransport.CountryPage(Index(1), dtmIndex), Country("IT", ""))
          .setValue(ItemDescriptionPage(Index(1), itemIndex), "shirts")
          .setValue(GrossWeightPage(Index(1), itemIndex), BigDecimal(123.45))
          .setValue(NetWeightPage(Index(1), itemIndex), BigDecimal(123.45))

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(8)

        section.sectionTitle.value mustBe "House consignments"
        section.children.length mustBe 2

        section.children.head.sectionTitle.value mustBe "House consignment 1"
        section.children.head.rows.size mustBe 2
        section.children.head.viewLinks.size mustBe 1

      }
    }

    "must render additional references sections" - {
      "when there is one" in {

        val userAnswers = emptyUserAnswers
          .setValue(AdditionalReferenceTypePage(index), AdditionalReferenceType("Y015", "The rough diamonds are contained in ..."))
          .setValue(AdditionalReferenceNumberPage(index), "addref-1")

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(5)

        section.sectionTitle.value mustBe "Additional references"
        section.children.head.rows.size mustBe 2
      }

      "when there are multiple" in {

        val userAnswers = emptyUserAnswers
          .setValue(AdditionalReferenceTypePage(Index(0)), AdditionalReferenceType("Y015", "The rough diamonds are contained in ..."))
          .setValue(AdditionalReferenceNumberPage(Index(0)), "addref-1")
          .setValue(AdditionalReferenceTypePage(Index(1)), AdditionalReferenceType("Y022", "Consignor / exporter (AEO certificate number)"))
          .setValue(AdditionalReferenceNumberPage(Index(1)), "addref-2")

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(5)

        section.sectionTitle.value mustBe "Additional references"
        section.children.size mustBe 2
        section.viewLinks must not be Nil
      }
    }

    "must render additional information sections" - {
      "when there is one" in {

        val userAnswers = emptyUserAnswers
          .setValue(AdditionalInformationCodePage(index), AdditionalInformationCode("20300", "Export"))
          .setValue(AdditionalInformationTextPage(index), "a string")

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(7)

        section.sectionTitle.value mustBe "Additional information"
        section.children.head.rows.size mustBe 2
      }

      "when there are multiple" in {

        val userAnswers = emptyUserAnswers
          .setValue(AdditionalInformationCodePage(Index(0)), AdditionalInformationCode("20300", "Export"))
          .setValue(AdditionalInformationTextPage(Index(0)), "a string")
          .setValue(
            AdditionalInformationCodePage(Index(1)),
            AdditionalInformationCode(
              "30600",
              "In EXS, where negotiable bills of lading 'to order blank endorsed' are concerned and the consignee particulars are unknown."
            )
          )
          .setValue(AdditionalInformationTextPage(Index(1)), "another string")

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(7)

        section.sectionTitle.value mustBe "Additional information"
        section.children.size mustBe 2
      }
    }

    "must render incidents sections" - {
      "when there is one" in {

        val userAnswers = emptyUserAnswers
          .setValue(IncidentCodePage(index), Incident("1", "bad wrapping paper"))
          .setValue(IncidentTextPage(index), "it got wet")

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.sections(6)

        section.sectionTitle.value mustBe "Incidents"
        section.children.length mustBe 1

        section.children.head.sectionTitle.value mustBe "Incident 1"
        section.children.head.rows.size mustBe 2
        section.children.head.viewLinks mustBe Nil
      }

      "when there are multiple" in {

        val userAnswers = emptyUserAnswers
          .setValue(IncidentCodePage(Index(0)), Incident("1", "desc 1"))
          .setValue(IncidentTextPage(Index(0)), "free text 1")
          .setValue(IncidentCodePage(Index(1)), Incident("2", "desc 2"))
          .setValue(IncidentTextPage(Index(1)), "free text 2")

        val viewModelProvider = app.injector.instanceOf[UnloadingFindingsViewModelProvider]
        val result            = viewModelProvider.apply(userAnswers)

        val section = result.sections(6)

        section.sectionTitle.value mustBe "Incidents"
        section.children.length mustBe 2

        section.children.head.sectionTitle.value mustBe "Incident 1"
        section.children.head.rows.size mustBe 2
        section.children.head.viewLinks mustBe Nil

        section.children(1).sectionTitle.value mustBe "Incident 2"
        section.children(1).rows.size mustBe 2
        section.children(1).viewLinks mustBe Nil
      }
    }
  }
}
