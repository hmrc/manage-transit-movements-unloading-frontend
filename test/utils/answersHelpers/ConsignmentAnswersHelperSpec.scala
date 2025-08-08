/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.answersHelpers

import generated.*
import models.DocType.Previous
import models.reference.*
import models.reference.TransportMode.InlandMode
import models.{CheckMode, Coordinates, Index, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.countriesOfRouting.CountryOfRoutingPage
import pages.transportEquipment.index.ItemPage
import viewModels.sections.Section.{AccordionSection, StaticSection}

import javax.xml.datatype.XMLGregorianCalendar

class ConsignmentAnswersHelperSpec extends AnswersHelperSpecBase {

  "ConsignmentAnswersHelper" - {

    "headerSection" - {
      import pages.*

      "must return static section" in {
        val ie043 = basicIe043.copy(
          TransitOperation = TransitOperationType10(
            MRN = Gen.alphaNumStr.sample.value,
            declarationType = Some(Gen.alphaNumStr.sample.value),
            declarationAcceptanceDate = Some(arbitrary[XMLGregorianCalendar].sample.value),
            security = Gen.alphaNumStr.sample.value,
            reducedDatasetIndicator = arbitrary[Flag].sample.value
          ),
          TraderAtDestination = arbitrary[TraderAtDestinationType02].sample.value
        )
        val answers = emptyUserAnswers
          .copy(ie043Data = ie043)
          .setValue(SecurityTypePage, arbitrary[SecurityType].sample.value)
          .setValue(CustomsOfficeOfDestinationActualPage, arbitrary[CustomsOffice].sample.value)
          .setValue(GrossWeightPage, arbitrary[BigDecimal].sample.value)

        val helper = new ConsignmentAnswersHelper(answers)
        val result = helper.headerSection

        result mustBe a[StaticSection]
        result.rows.size mustEqual 7
      }
    }

    "traderAtDestinationRow" - {
      "must return row" in {
        forAll(Gen.alphaNumStr) {
          value =>
            val traderAtDestination = TraderAtDestinationType02(value)
            val ie043               = basicIe043.copy(TraderAtDestination = traderAtDestination)
            val answers             = emptyUserAnswers.copy(ie043Data = ie043)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.traderAtDestinationRow

            result.key.value mustEqual "Authorised consignee’s EORI number or Trader Identification Number (TIN)"
            result.value.value mustEqual value
            result.actions must not be defined
        }
      }
    }

    "consignorSection" - {

      "must return no section" - {
        "when consignor is undefined" in {
          forAll(arbitrary[ConsignmentType05]) {
            consignment =>
              val userAnswers = emptyUserAnswers.copy(
                ie043Data = basicIe043.copy(
                  Consignment = Some(
                    consignment.copy(
                      Consignor = None
                    )
                  )
                )
              )

              val helper = new ConsignmentAnswersHelper(userAnswers)
              helper.consignorSection must not be defined
          }
        }
      }

      "must return section" - {
        "when consignor is defined" in {
          forAll(arbitrary[ConsignmentType05], arbitrary[ConsignorType04]) {
            (consignment, consignor) =>
              val userAnswers = emptyUserAnswers.copy(
                ie043Data = basicIe043.copy(
                  Consignment = Some(
                    consignment.copy(
                      Consignor = Some(consignor)
                    )
                  )
                )
              )

              val helper = new ConsignmentAnswersHelper(userAnswers)
              val result = helper.consignorSection.value

              result.sectionTitle.value mustEqual "Consignor"
          }
        }
      }
    }

    "consigneeSection" - {

      "must return no section" - {
        "when consignee is undefined" in {
          forAll(arbitrary[ConsignmentType05]) {
            consignment =>
              val userAnswers = emptyUserAnswers.copy(
                ie043Data = basicIe043.copy(
                  Consignment = Some(
                    consignment.copy(
                      Consignee = None
                    )
                  )
                )
              )

              val helper = new ConsignmentAnswersHelper(userAnswers)
              helper.consigneeSection must not be defined
          }
        }
      }

      "must return section" - {
        "when consignee is defined" in {
          forAll(arbitrary[ConsignmentType05], arbitrary[ConsigneeType05]) {
            (consignment, consignee) =>
              val userAnswers = emptyUserAnswers.copy(
                ie043Data = basicIe043.copy(
                  Consignment = Some(
                    consignment.copy(
                      Consignee = Some(consignee)
                    )
                  )
                )
              )

              val helper = new ConsignmentAnswersHelper(userAnswers)
              val result = helper.consigneeSection.value

              result.sectionTitle.value mustEqual "Consignee"
          }
        }
      }
    }

    "countriesOfRouting" - {
      "must generate accordion section" in {
        forAll(arbitrary[Country], arbitrary[Country]) {
          (value1, value2) =>
            val answers = emptyUserAnswers
              .setValue(CountryOfRoutingPage(Index(0)), value1)
              .setValue(CountryOfRoutingPage(Index(1)), value2)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.countriesOfRoutingSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Countries of routing"
            result.id.value mustEqual "countries-of-routing"

            val addOrRemoveLink = result.viewLinks.head
            addOrRemoveLink.id mustEqual "add-remove-countries-of-routing"
            addOrRemoveLink.text mustEqual "Add or remove country of routing"
            addOrRemoveLink.visuallyHidden must not be defined
            addOrRemoveLink.href mustEqual controllers.countriesOfRouting.routes.AddAnotherCountryController.onPageLoad(arrivalId, NormalMode).url

            result.rows.size mustEqual 2
            result.rows.head.value.value mustEqual value1.toString
            result.rows(1).value.value mustEqual value2.toString
        }
      }
    }

    "grossMassRow" - {
      import pages.GrossWeightPage

      "must return None" - {
        s"when no transport equipments defined" in {
          val helper = new ConsignmentAnswersHelper(emptyUserAnswers)
          val result = helper.grossMassRow
          result.isEmpty mustEqual true
        }
      }

      "must return Some(Row)" - {
        s"when $GrossWeightPage is defined" in {
          val answers = emptyUserAnswers
            .setValue(GrossWeightPage, BigDecimal(999.99))

          val helper = new ConsignmentAnswersHelper(answers)
          val result = helper.grossMassRow.value

          result.key.value mustEqual "Gross weight"
          result.value.value mustEqual "999.99kg"
          val action = result.actions.value.items.head
          action.content.value mustEqual "Change"
          action.href mustEqual controllers.routes.GrossWeightController.onPageLoad(arrivalId).url
          action.visuallyHiddenText.value mustEqual "gross weight"
          action.id mustEqual "change-gross-mass"
        }
      }
    }

    "ucrRow" - {
      import pages.UniqueConsignmentReferencePage

      "must return None" - {
        s"when $UniqueConsignmentReferencePage is undefined" in {
          val helper = new ConsignmentAnswersHelper(emptyUserAnswers)
          val result = helper.ucrRow
          result.isEmpty mustEqual true
        }
      }

      "must return Some(Row)" - {
        s"when $UniqueConsignmentReferencePage is defined" in {
          val answers = emptyUserAnswers
            .setValue(UniqueConsignmentReferencePage, "foo")

          val helper = new ConsignmentAnswersHelper(answers)
          val result = helper.ucrRow.value

          result.key.value mustEqual "Reference number UCR"
          result.value.value mustEqual "foo"
          val action = result.actions.value.items.head
          action.content.value mustEqual "Change"
          action.href mustEqual controllers.routes.UniqueConsignmentReferenceController.onPageLoad(arrivalId).url
          action.visuallyHiddenText.value mustEqual "reference number UCR"
          action.id mustEqual "change-unique-consignment-reference"
        }
      }
    }

    "holderOfTheTransitProcedureSection" - {
      import pages.holderOfTheTransitProcedure.CountryPage

      "must return empty when HolderOfTheTransitProcedure is undefined" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = None))

        val helper = new ConsignmentAnswersHelper(userAnswers)
        helper.holderOfTheTransitProcedureSection must not be defined
      }

      "must return section title and rows when HolderOfTheTransitProcedure is defined" in {
        val holderOfTransit = HolderOfTheTransitProcedureType06(
          Some("identificationNumber"),
          Some("TIRHolderIdentificationNumber"),
          "name",
          AddressType15("streetAndNumber", Some("postcode"), "city", "GB")
        )

        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = Some(holderOfTransit)))
          .setValue(CountryPage, Country("GB", "Great Britain"))

        val helper  = new ConsignmentAnswersHelper(userAnswers)
        val section = helper.holderOfTheTransitProcedureSection.head

        section mustBe a[StaticSection]

        section.sectionTitle.value mustEqual "Transit holder"
        section.rows.size mustEqual 5
        section.viewLinks mustEqual Nil
      }
    }

    "departureTransportMeansSections" - {
      import pages.departureMeansOfTransport.*

      "must generate accordion sections" in {
        forAll(arbitrary[TransportMeansIdentification], Gen.alphaNumStr, arbitrary[Country]) {
          (`type`, number, country) =>
            val answers = emptyUserAnswers
              .setValue(TransportMeansIdentificationPage(dtmIndex), `type`)
              .setValue(VehicleIdentificationNumberPage(dtmIndex), number)
              .setValue(CountryPage(dtmIndex), country)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.departureTransportMeansSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Departure means of transport"
            result.viewLinks.head.href mustEqual
              controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onPageLoad(arrivalId, NormalMode).url

            result.children.head mustBe a[AccordionSection]
            result.children.head.sectionTitle.value mustEqual "Departure means of transport 1"
            result.children.head.rows.size mustEqual 3
            result.children.head.rows.head.value.value mustEqual `type`.description
            result.children.head.rows(1).value.value mustEqual number
            result.children.head.rows(2).value.value mustEqual country.description

        }
      }
    }

    "transportEquipmentSections" - {
      import pages.*
      import pages.transportEquipment.index.seals.SealIdentificationNumberPage

      "must generate accordion section with accordion and static children sections" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[BigInt]) {
          (containerId, sealId, item) =>
            val answers = emptyUserAnswers
              .setValue(ContainerIdentificationNumberPage(equipmentIndex), containerId)
              .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), sealId)
              .setValue(ItemPage(equipmentIndex, itemIndex), item)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.transportEquipmentSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Transport equipment"
            result.viewLinks.head.href mustEqual controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(arrivalId, NormalMode).url
            result.children.head mustBe a[AccordionSection]
            result.children.head.sectionTitle.value mustEqual "Transport equipment 1"

            result.children.head.rows.size mustEqual 1
            result.children.head.rows.head.value.value mustEqual containerId

            result.children.head.children.head mustBe a[AccordionSection]
            result.children.head.children.head.rows.size mustEqual 1
            result.children.head.children.head.rows.head.value.value mustEqual sealId
            result.children.head.children.head.viewLinks.head.href mustEqual
              controllers.transportEquipment.index.routes.AddAnotherSealController.onPageLoad(arrivalId, CheckMode, equipmentIndex).url

            result.children.head.children(1) mustBe a[AccordionSection]
            result.children.head.children(1).rows.size mustEqual 1
            result.children.head.children(1).rows.head.value.value mustEqual item.toString
            result.children.head.children(1).viewLinks.head.href mustEqual
              controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(arrivalId, CheckMode, equipmentIndex).url
        }
      }
    }

    "additionalReferencesSections" - {
      import pages.additionalReference.*

      "must generate accordion sections" in {
        forAll(arbitrary[AdditionalReferenceType], Gen.alphaNumStr) {
          (`type`, number) =>
            val answers = emptyUserAnswers
              .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), `type`)
              .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), number)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.additionalReferencesSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Additional references"
            result.children.size mustEqual 1
            result.children.head.sectionTitle.value mustEqual "Additional reference 1"
            result.children.head.rows.size mustEqual 2
            result.children.head.rows.head.value.value mustEqual `type`.toString
            result.children.head.rows(1).value.value mustEqual number
            result.viewLinks.head.href mustEqual "/manage-transit-movements/unloading/AB123/additional-references/add-another"
            result.id.value mustEqual "additionalReferences"
        }
      }
    }

    "additionalInformationSections" - {
      import pages.additionalInformation.*

      "must generate accordion section with children when additional information defined" in {
        forAll(arbitrary[AdditionalInformationCode], Gen.alphaNumStr) {
          (code, text) =>
            val answers = emptyUserAnswers
              .setValue(AdditionalInformationCodePage(index), code)
              .setValue(AdditionalInformationTextPage(index), text)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.additionalInformationSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Additional information"
            result.children.size mustEqual 1
            result.children.head.sectionTitle.value mustEqual "Additional information 1"
            result.children.head.rows.size mustEqual 2
            result.children.head.rows.head.value.value mustEqual code.toString
            result.children.head.rows(1).value.value mustEqual text
            result.viewLinks mustBe empty
            result.id.value mustEqual "additionalInformation"
        }
      }

      "must generate accordion section with no children when additional information undefined" in {
        val helper = new ConsignmentAnswersHelper(emptyUserAnswers)
        val result = helper.additionalInformationSection

        result.children.size mustEqual 0
      }
    }

    "documentSections" - {
      import pages.documents.*

      "must generate accordion sections" in {
        forAll(arbitrary[DocumentType], Gen.alphaNumStr, Gen.alphaNumStr) {
          (documentType, referenceNumber, additionalInformation) =>
            val previousDoc = documentType.copy(`type` = Previous)
            val answers = emptyUserAnswers
              .setValue(TypePage(Index(0)), documentType)
              .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)
              .setValue(AdditionalInformationPage(Index(0)), additionalInformation)
              .setValue(TypePage(Index(1)), documentType)
              .setValue(DocumentReferenceNumberPage(Index(1)), referenceNumber)
              .setValue(AdditionalInformationPage(Index(1)), additionalInformation)
              .setValue(TypePage(Index(2)), previousDoc)
              .setValue(DocumentReferenceNumberPage(Index(2)), referenceNumber)
              .setValue(AdditionalInformationPage(Index(2)), additionalInformation)

            val helper = new ConsignmentAnswersHelper(answers)
            val result = helper.documentSection

            result mustBe a[AccordionSection]
            result.sectionTitle.value mustEqual "Documents"
            result.viewLinks.head.href mustEqual controllers.documents.routes.AddAnotherDocumentController.onPageLoad(arrivalId, NormalMode).url

            result.children.head.sectionTitle.value mustEqual "Document 1"
            result.children.head.rows.size mustEqual 3
            result.children.head.rows.head.value.value mustEqual documentType.toString
            result.children.head.rows(1).value.value mustEqual referenceNumber
            result.children.head.rows(2).value.value mustEqual additionalInformation
            result.children.head.viewLinks mustEqual Nil

            result.children(1).sectionTitle.value mustEqual "Document 2"
            result.children(1).rows.size mustEqual 3
            result.children(1).rows.head.value.value mustEqual documentType.toString
            result.children(1).rows(1).value.value mustEqual referenceNumber
            result.children(1).rows(2).value.value mustEqual additionalInformation
            result.children(1).viewLinks mustEqual Nil

            result.children(2).sectionTitle.value mustEqual "Document 3"
            result.children(2).rows.size mustEqual 3
            result.children(2).rows.head.value.value mustEqual previousDoc.toString
            result.children(2).rows(1).value.value mustEqual referenceNumber
            result.children(2).rows(1).actions must not be defined
            result.children(2).rows(2).value.value mustEqual additionalInformation
            result.children(2).rows(2).actions must not be defined
            result.children(2).viewLinks mustEqual Nil
        }
      }
    }

    "houseConsignmentSections" - {
      import pages.*
      "and there are house consignments" - {
        "must generate accordion sections with add/remove link" in {
          forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
            (consignorName, consignorId, consigneeName, consigneeId) =>
              val answers = emptyUserAnswers
                .setValue(ConsignorNamePage(hcIndex), consignorName)
                .setValue(ConsignorIdentifierPage(hcIndex), consignorId)
                .setValue(ConsigneeNamePage(hcIndex), consigneeName)
                .setValue(ConsigneeIdentifierPage(hcIndex), consigneeId)

              val helper = new ConsignmentAnswersHelper(answers)(
                messages
              )
              val result = helper.houseConsignmentSection

              result mustBe a[AccordionSection]
              result.sectionTitle.value mustEqual "House consignments"

              result.viewLinks.size mustEqual 1
              val addOrRemoveLink = result.viewLinks.head
              addOrRemoveLink.id mustEqual "add-remove-house-consignment"
              addOrRemoveLink.text mustEqual "Add or remove house consignment"
              addOrRemoveLink.visuallyHidden must not be defined
              addOrRemoveLink.href mustEqual controllers.houseConsignment.routes.AddAnotherHouseConsignmentController.onPageLoad(arrivalId, NormalMode).url

              result.children.head mustBe a[AccordionSection]
              result.children.head.sectionTitle.value mustEqual "House consignment 1"
              result.children.head.rows.size mustEqual 2

              val link = result.children.head.viewLinks.head
              link.id mustEqual "view-house-consignment-1"
              link.text mustEqual "More details"
              link.href mustEqual controllers.routes.HouseConsignmentController.onPageLoad(answers.id, hcIndex).url
              link.visuallyHidden.value mustEqual "on house consignment 1"
              result.children.head.id.value mustEqual "houseConsignment1"
          }
        }
      }

      "and there are no house consignments" - {
        "must generate add/remove link" in {
          val answers = emptyUserAnswers
          val helper = new ConsignmentAnswersHelper(answers)(
            messages
          )
          val result = helper.houseConsignmentSection

          result mustBe a[AccordionSection]
          result.sectionTitle.value mustEqual "House consignments"

          result.children mustEqual Nil

          result.viewLinks.size mustEqual 1
          val addOrRemoveLink = result.viewLinks.head
          addOrRemoveLink.id mustEqual "add-remove-house-consignment"
          addOrRemoveLink.text mustEqual "Add or remove house consignment"
          addOrRemoveLink.visuallyHidden must not be defined
          addOrRemoveLink.href mustEqual controllers.houseConsignment.routes.AddAnotherHouseConsignmentController.onPageLoad(arrivalId, NormalMode).url
        }
      }
    }

    "incidentSection" - {
      import pages.incident.*
      import pages.incident.endorsement.*
      import pages.incident.location.*

      "must generate accordion sections" in {
        val incident           = arbitrary[IncidentType03].sample.value
        val endorsement        = arbitrary[EndorsementType02].sample.value
        val inc                = arbitrary[Incident].sample.value
        val qualifier          = arbitrary[QualifierOfIdentification].sample.value
        val country            = arbitrary[Country].sample.value
        val LocationType       = arbitrary[LocationType].sample.value
        val coordinate         = arbitrary[Coordinates].sample.value
        val unLocode           = Gen.alphaNumStr.sample.value
        val description        = Gen.alphaNumStr.sample.value
        val arbitraryTransport = arbitrary[TransportEquipmentType06].sample.value

        val address = AddressType21("streetAndNumber", Some("postcode"), "city")

        val locationType = LocationType.copy(
          UNLocode = Some(unLocode),
          GNSS = Some(GNSSType(coordinate.latitude, coordinate.longitude)),
          Address = Some(address)
        )
        val consignment = ConsignmentType05(
          containerIndicator = Number0,
          Incident = Seq(incident.copy(Endorsement = Some(endorsement), Location = locationType, TransportEquipment = Seq(arbitraryTransport)))
        )

        val answers = emptyUserAnswers
          .copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))
          .setValue(CountryPage(index), country)
          .setValue(IncidentCodePage(index), inc)
          .setValue(IncidentTextPage(index), description)
          .setValue(QualifierOfIdentificationPage(index), qualifier)
          .setValue(EndorsementCountryPage(index), country)

        val helper = new ConsignmentAnswersHelper(answers)
        val result = helper.incidentSection

        result mustBe a[AccordionSection]
        result.sectionTitle.value mustEqual "Incidents"

        result mustBe a[AccordionSection]
        result.children.head.sectionTitle.value mustEqual "Incident 1"
        result.children.head.rows.size mustEqual 8
        result.children.head.rows.head.value.value mustEqual country.toString
        result.children.head.rows(1).value.value mustEqual inc.toString
        result.children.head.rows(2).value.value mustEqual description
        result.children.head.rows(3).value.value mustEqual qualifier.toString
        result.children.head.rows(4).value.value mustEqual coordinate.toString
        result.children.head.rows(5).value.value mustEqual unLocode
        result.children.head.rows(6).value.value mustEqual s"${address.streetAndNumber}<br>${address.city}<br>${address.postcode.get}"

        result.children.head.children.head.sectionTitle.value mustEqual "Endorsements"
        result.children.head.children.head.rows.head.key.value mustEqual "Endorsement date"
        result.children.head.children.head.rows(1).key.value mustEqual "Authority"
        result.children.head.children.head.rows(2).key.value mustEqual "Country"
        result.children.head.children.head.rows(3).key.value mustEqual "Location"
        result.children.head.children.head.sectionTitle.value mustEqual "Endorsements"
        result.children.head.children.head.rows.head.key.value mustEqual "Endorsement date"
        result.children.head.children.head.rows(1).key.value mustEqual "Authority"
        result.children.head.children.head.rows(2).key.value mustEqual "Country"
        result.children.head.children.head.rows(3).key.value mustEqual "Location"
        result.children.head.children.head.rows(3).key.value mustEqual "Location"
      }
    }

    "inlandModeOfTransportRow" - {
      import pages.inlandModeOfTransport.InlandModeOfTransportPage

      "must return None" - {
        s"when no inland mode of transport defined" in {
          val helper = new ConsignmentAnswersHelper(emptyUserAnswers)
          val result = helper.inlandModeOfTransportRow
          result.isEmpty mustEqual true
        }
      }

      "must return Some(Row)" - {
        s"when $InlandModeOfTransportPage is defined" in {
          val answers = emptyUserAnswers
            .setValue(InlandModeOfTransportPage, InlandMode("1", "Maritime Transport"))

          val helper = new ConsignmentAnswersHelper(answers)
          val result = helper.inlandModeOfTransportRow.value

          result.key.value mustEqual "Mode"
          result.value.value mustEqual "Maritime Transport"
        }
      }
    }

    "countryOfDestinationRow" - {
      import pages.countryOfDestination.CountryOfDestinationPage

      "must return None" - {
        s"when no country of destination defined" in {
          val helper = new ConsignmentAnswersHelper(emptyUserAnswers)
          val result = helper.countryOfDestinationRow
          result.isEmpty mustEqual true
        }
      }

      "must return Some(Row)" - {
        s"when $CountryOfDestinationPage is defined" in {
          val answers = emptyUserAnswers
            .setValue(CountryOfDestinationPage, Country("FR", "France"))

          val helper = new ConsignmentAnswersHelper(answers)
          val result = helper.countryOfDestinationRow.value

          result.key.value mustEqual "Country of destination"
          result.value.value mustEqual "France - FR"
        }
      }
    }

  }
}
