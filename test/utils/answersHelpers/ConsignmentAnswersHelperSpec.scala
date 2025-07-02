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
        result.rows.size mustBe 7
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

            result.key.value mustBe "Authorised consignee’s EORI number or Trader Identification Number (TIN)"
            result.value.value mustBe value
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

              result.sectionTitle.value mustBe "Consignor"
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

              result.sectionTitle.value mustBe "Consignee"
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
            result.sectionTitle.value mustBe "Countries of routing"
            result.id.value mustBe "countries-of-routing"

            val addOrRemoveLink = result.viewLinks.head
            addOrRemoveLink.id mustBe "add-remove-countries-of-routing"
            addOrRemoveLink.text mustBe "Add or remove country of routing"
            addOrRemoveLink.visuallyHidden must not be defined
            addOrRemoveLink.href mustBe controllers.countriesOfRouting.routes.AddAnotherCountryController.onPageLoad(arrivalId, NormalMode).url

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
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)" - {
        s"when $GrossWeightPage is defined" in {
          val answers = emptyUserAnswers
            .setValue(GrossWeightPage, BigDecimal(999.99))

          val helper = new ConsignmentAnswersHelper(answers)
          val result = helper.grossMassRow.value

          result.key.value mustBe "Gross weight"
          result.value.value mustBe "999.99kg"
          val action = result.actions.value.items.head
          action.content.value mustBe "Change"
          action.href mustBe controllers.routes.GrossWeightController.onPageLoad(arrivalId).url
          action.visuallyHiddenText.value mustBe "gross weight"
          action.id mustBe "change-gross-mass"
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

          result.key.value mustBe "Reference number UCR"
          result.value.value mustBe "foo"
          val action = result.actions.value.items.head
          action.content.value mustBe "Change"
          action.href mustBe controllers.routes.UniqueConsignmentReferenceController.onPageLoad(arrivalId, CheckMode).url
          action.visuallyHiddenText.value mustBe "reference number UCR"
          action.id mustBe "change-unique-consignment-reference"
        }
      }
    }

    "holderOfTheTransitProcedureSection" - {
      import pages.holderOfTheTransitProcedure.CountryPage

      "must return empty when HolderOfTheTransitProcedure is undefined" in {
        val userAnswers = emptyUserAnswers
          .copy(ie043Data = basicIe043.copy(HolderOfTheTransitProcedure = None))

        val helper = new ConsignmentAnswersHelper(userAnswers)
        helper.holderOfTheTransitProcedureSection mustBe None
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

        section.sectionTitle.value mustBe "Transit holder"
        section.rows.size mustBe 5
        section.viewLinks mustBe Nil
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
            result.sectionTitle.value mustBe "Departure means of transport"
            result.viewLinks.head.href mustBe
              controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onPageLoad(arrivalId, NormalMode).url

            result.children.head mustBe a[AccordionSection]
            result.children.head.sectionTitle.value mustBe "Departure means of transport 1"
            result.children.head.rows.size mustBe 3
            result.children.head.rows.head.value.value mustBe `type`.description
            result.children.head.rows(1).value.value mustBe number
            result.children.head.rows(2).value.value mustBe country.description

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
            result.sectionTitle.value mustBe "Transport equipment"
            result.viewLinks.head.href mustBe controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(arrivalId, NormalMode).url
            result.children.head mustBe a[AccordionSection]
            result.children.head.sectionTitle.value mustBe "Transport equipment 1"

            result.children.head.rows.size mustBe 1
            result.children.head.rows.head.value.value mustBe containerId

            result.children.head.children.head mustBe a[AccordionSection]
            result.children.head.children.head.rows.size mustBe 1
            result.children.head.children.head.rows.head.value.value mustBe sealId
            result.children.head.children.head.viewLinks.head.href mustBe
              controllers.transportEquipment.index.routes.AddAnotherSealController.onPageLoad(arrivalId, CheckMode, equipmentIndex).url

            result.children.head.children(1) mustBe a[AccordionSection]
            result.children.head.children(1).rows.size mustBe 1
            result.children.head.children(1).rows.head.value.value mustBe item.toString
            result.children.head.children(1).viewLinks.head.href mustBe
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
            result.sectionTitle.value mustBe "Additional references"
            result.children.size mustBe 1
            result.children.head.sectionTitle.value mustBe "Additional reference 1"
            result.children.head.rows.size mustBe 2
            result.children.head.rows.head.value.value mustBe `type`.toString
            result.children.head.rows(1).value.value mustBe number
            result.viewLinks.head.href mustBe "/manage-transit-movements/unloading/AB123/additional-references/add-another"
            result.id.value mustBe "additionalReferences"
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
            result.sectionTitle.value mustBe "Additional information"
            result.children.size mustBe 1
            result.children.head.sectionTitle.value mustBe "Additional information 1"
            result.children.head.rows.size mustBe 2
            result.children.head.rows.head.value.value mustBe code.toString
            result.children.head.rows(1).value.value mustBe text
            result.viewLinks mustBe empty
            result.id.value mustBe "additionalInformation"
        }
      }

      "must generate accordion section with no children when additional information undefined" in {
        val helper = new ConsignmentAnswersHelper(emptyUserAnswers)
        val result = helper.additionalInformationSection

        result.children.size mustBe 0
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
            result.sectionTitle.value mustBe "Documents"
            result.viewLinks.head.href mustBe controllers.documents.routes.AddAnotherDocumentController.onPageLoad(arrivalId, NormalMode).url

            result.children.head.sectionTitle.value mustBe "Document 1"
            result.children.head.rows.size mustBe 3
            result.children.head.rows.head.value.value mustBe documentType.toString
            result.children.head.rows(1).value.value mustBe referenceNumber
            result.children.head.rows(2).value.value mustBe additionalInformation
            result.children.head.viewLinks mustBe Nil

            result.children(1).sectionTitle.value mustBe "Document 2"
            result.children(1).rows.size mustBe 3
            result.children(1).rows.head.value.value mustBe documentType.toString
            result.children(1).rows(1).value.value mustBe referenceNumber
            result.children(1).rows(2).value.value mustBe additionalInformation
            result.children(1).viewLinks mustBe Nil

            result.children(2).sectionTitle.value mustBe "Document 3"
            result.children(2).rows.size mustBe 3
            result.children(2).rows.head.value.value mustBe previousDoc.toString
            result.children(2).rows(1).value.value mustBe referenceNumber
            result.children(2).rows(1).actions mustBe None
            result.children(2).rows(2).value.value mustBe additionalInformation
            result.children(2).rows(2).actions mustBe None
            result.children(2).viewLinks mustBe Nil
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
              result.sectionTitle.value mustBe "House consignments"

              result.viewLinks.size mustBe 1
              val addOrRemoveLink = result.viewLinks.head
              addOrRemoveLink.id mustBe "add-remove-house-consignment"
              addOrRemoveLink.text mustBe "Add or remove house consignment"
              addOrRemoveLink.visuallyHidden must not be defined
              addOrRemoveLink.href mustBe controllers.houseConsignment.routes.AddAnotherHouseConsignmentController.onPageLoad(arrivalId, NormalMode).url

              result.children.head mustBe a[AccordionSection]
              result.children.head.sectionTitle.value mustBe "House consignment 1"
              result.children.head.rows.size mustBe 2

              val link = result.children.head.viewLinks.head
              link.id mustBe "view-house-consignment-1"
              link.text mustBe "More details"
              link.href mustBe controllers.routes.HouseConsignmentController.onPageLoad(answers.id, hcIndex).url
              link.visuallyHidden.value mustBe "on house consignment 1"
              result.children.head.id.value mustBe "houseConsignment1"
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
          result.sectionTitle.value mustBe "House consignments"

          result.children mustBe Nil

          result.viewLinks.size mustBe 1
          val addOrRemoveLink = result.viewLinks.head
          addOrRemoveLink.id mustBe "add-remove-house-consignment"
          addOrRemoveLink.text mustBe "Add or remove house consignment"
          addOrRemoveLink.visuallyHidden must not be defined
          addOrRemoveLink.href mustBe controllers.houseConsignment.routes.AddAnotherHouseConsignmentController.onPageLoad(arrivalId, NormalMode).url
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
        result.sectionTitle.value mustBe "Incidents"

        result mustBe a[AccordionSection]
        result.children.head.sectionTitle.value mustBe "Incident 1"
        result.children.head.rows.size mustBe 8
        result.children.head.rows.head.value.value mustBe country.toString
        result.children.head.rows(1).value.value mustBe inc.toString
        result.children.head.rows(2).value.value mustBe description
        result.children.head.rows(3).value.value mustBe qualifier.toString
        result.children.head.rows(4).value.value mustBe coordinate.toString
        result.children.head.rows(5).value.value mustBe unLocode
        result.children.head.rows(6).value.value mustBe s"${address.streetAndNumber}<br>${address.city}<br>${address.postcode.get}"

        result.children.head.children.head.sectionTitle.value mustBe "Endorsements"
        result.children.head.children.head.rows.head.key.value mustBe "Endorsement date"
        result.children.head.children.head.rows(1).key.value mustBe "Authority"
        result.children.head.children.head.rows(2).key.value mustBe "Country"
        result.children.head.children.head.rows(3).key.value mustBe "Location"
        result.children.head.children.head.sectionTitle.value mustBe "Endorsements"
        result.children.head.children.head.rows.head.key.value mustBe "Endorsement date"
        result.children.head.children.head.rows(1).key.value mustBe "Authority"
        result.children.head.children.head.rows(2).key.value mustBe "Country"
        result.children.head.children.head.rows(3).key.value mustBe "Location"
        result.children.head.children.head.rows(3).key.value mustBe "Location"
      }
    }

    "inlandModeOfTransportRow" - {
      import pages.inlandModeOfTransport.InlandModeOfTransportPage

      "must return None" - {
        s"when no inland mode of transport defined" in {
          val helper = new ConsignmentAnswersHelper(emptyUserAnswers)
          val result = helper.inlandModeOfTransportRow
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)" - {
        s"when $InlandModeOfTransportPage is defined" in {
          val answers = emptyUserAnswers
            .setValue(InlandModeOfTransportPage, InlandMode("1", "Maritime Transport"))

          val helper = new ConsignmentAnswersHelper(answers)
          val result = helper.inlandModeOfTransportRow.value

          result.key.value mustBe "Mode"
          result.value.value mustBe "Maritime Transport"
        }
      }
    }

    "countryOfDestinationRow" - {
      import pages.countryOfDestination.CountryOfDestinationPage

      "must return None" - {
        s"when no country of destination defined" in {
          val helper = new ConsignmentAnswersHelper(emptyUserAnswers)
          val result = helper.countryOfDestinationRow
          result.isEmpty mustBe true
        }
      }

      "must return Some(Row)" - {
        s"when $CountryOfDestinationPage is defined" in {
          val answers = emptyUserAnswers
            .setValue(CountryOfDestinationPage, Country("FR", "France"))

          val helper = new ConsignmentAnswersHelper(answers)
          val result = helper.countryOfDestinationRow.value

          result.key.value mustBe "Country of destination"
          result.value.value mustBe "France - FR"
        }
      }
    }

  }
}
