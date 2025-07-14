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

package utils.answersHelpers.consignment

import generated._
import models.Coordinates
import models.reference.{Country, Incident, QualifierOfIdentification, TransportMeansIdentification}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.incident.endorsement.EndorsementCountryPage
import pages.incident.location._
import pages.incident.replacementMeansOfTransport.{IdentificationPage, NationalityPage}
import pages.incident.{IncidentCodePage, IncidentTextPage}
import utils.Format.cyaDateFormatter
import utils.answersHelpers.AnswersHelperSpecBase
import utils.answersHelpers.consignment.incident.IncidentAnswersHelper

class IncidentAnswersHelperSpec extends AnswersHelperSpecBase {

  "IncidentAnswersHelper" - {

    "incidentCountryRow" - {
      val page = CountryPage(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentCountryRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentCountryRow.value

              result.key.value mustEqual "Country"
              result.value.value mustEqual value.toString
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentCodeRow" - {
      val page = IncidentCodePage(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentCodeRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Incident]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentCodeRow.value

              result.key.value mustEqual "Incident code"
              result.value.value mustEqual s"${value.toString}"
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentDescriptionRow" - {
      val page = IncidentTextPage(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentDescriptionRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentDescriptionRow.value

              result.key.value mustEqual "Description"
              result.value.value mustEqual s"$value"
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentQualifierRow" - {
      val page = QualifierOfIdentificationPage(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentQualifierRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[QualifierOfIdentification]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentQualifierRow.value

              result.key.value mustEqual "Identifier type"
              result.value.value mustEqual s"${value.toString}"
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentEndorsementDateRow" - {
      "must return None" - {
        s"when endorsement undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentDescriptionRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when endorsement defined" in {
          forAll(arbitrary[IncidentType03], arbitrary[EndorsementType02]) {
            (incident, endorsement) =>
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Endorsement = Some(endorsement)))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentEndorsementDateRow.value

              result.key.value mustEqual "Endorsement date"
              result.value.value mustEqual s"${endorsement.date.toGregorianCalendar.toZonedDateTime.format(cyaDateFormatter)}"
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentEndorsementAuthorityRow" - {
      "must return None" - {
        s"when endorsement undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentEndorsementAuthorityRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when endorsement defined" in {
          forAll(arbitrary[IncidentType03], arbitrary[EndorsementType02]) {
            (incident, endorsement) =>
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Endorsement = Some(endorsement)))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentEndorsementAuthorityRow.value

              result.key.value mustEqual "Authority"
              result.value.value mustEqual s"${endorsement.authority}"
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentEndorsementPlaceRow" - {
      "must return None" - {
        s"when endorsement undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentEndorsementPlaceRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when endorsement defined" in {
          forAll(arbitrary[IncidentType03], arbitrary[EndorsementType02]) {
            (incident, endorsement) =>
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Endorsement = Some(endorsement)))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentEndorsementPlaceRow.value

              result.key.value mustEqual "Location"
              result.value.value mustEqual s"${endorsement.place}"
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentEndorsementCountryRow" - {
      val page = EndorsementCountryPage(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentEndorsementCountryRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentEndorsementCountryRow.value

              result.key.value mustEqual "Country"
              result.value.value mustEqual s"${value.description}"
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentLocationAddressRow" - {
      "must return None" - {
        s"when location address undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentLocationAddressRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when location address defined" in {
          forAll(arbitrary[IncidentType03], arbitrary[LocationType]) {
            (incident, location) =>
              val address = Some(AddressType21("streetAndNumber", Some("postcode"), "city"))
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(
                  incident.copy(Location = LocationType(location.qualifierOfIdentification, location.UNLocode, location.country, location.GNSS, address))
                )
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentLocationAddressRow.value

              result.key.value mustEqual "Address"
              result.value.value mustEqual s"${address.value.streetAndNumber}<br>${address.value.city}<br>${address.value.postcode.get}"
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentCoordinatesRow" - {
      "must return None" - {
        s"when incident undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentCoordinatesRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when incident defined" in {
          forAll(arbitrary[IncidentType03], arbitrary[Coordinates], arbitrary[LocationType]) {
            (incident, coordinate, LocationType) =>
              val locationType = LocationType.copy(GNSS = Some(GNSSType(coordinate.latitude, coordinate.longitude)))
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Location = locationType))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentCoordinatesRow.value

              result.key.value mustEqual "Coordinates"
              result.value.value mustEqual s"$coordinate"
              val action = result.actions
              action must not be defined
          }
        }
      }
    }
    "containerIndicator" - {
      "must return None" - {
        s"when Container Indicator undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.containerIndicator must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when  Container Indicator  defined" in {

          forAll(arbitrary[Flag]) {
            containerIndicator =>
              val consignment = ConsignmentType05(containerIndicator = containerIndicator)
              val answers     = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))
              val helper      = new IncidentAnswersHelper(answers, index)
              val result      = helper.containerIndicator.value

              result.key.value mustEqual "Are you using any containers?"
              result.value.value.contains(containerIndicator.toString)
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentUnLocodeRow" - {
      "must return None" - {
        s"when incident undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentUnLocodeRow must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when incident defined" in {
          forAll(arbitrary[IncidentType03], Gen.alphaNumStr, arbitrary[LocationType]) {
            (incident, unLocode, LocationType) =>
              val locationType = LocationType.copy(UNLocode = Some(unLocode))
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Location = locationType))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentUnLocodeRow.value

              result.key.value mustEqual "UN/LOCODE"
              result.value.value mustEqual unLocode
              val action = result.actions
              action must not be defined
          }
        }
      }
    }

    "incidentTransportEquipments" - {
      "when no transport equipment" - {
        "must return no children" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          val result = helper.incidentTransportEquipments
          result.children.size mustEqual 0
        }
      }

      "when there are transport equipment" - {
        "must return children" in {
          val transportEquipment = TransportEquipmentType06(
            sequenceNumber = 1,
            containerIdentificationNumber = Some("cin"),
            numberOfSeals = Some(2),
            Seal = Seq(
              SealType01(
                sequenceNumber = 1,
                identifier = "seal1"
              ),
              SealType01(
                sequenceNumber = 2,
                identifier = "seal2"
              )
            ),
            GoodsReference = Seq(
              GoodsReferenceType03(
                sequenceNumber = 1,
                declarationGoodsItemNumber = 1
              ),
              GoodsReferenceType03(
                sequenceNumber = 2,
                declarationGoodsItemNumber = 2
              )
            )
          )

          forAll(arbitrary[IncidentType03]) {
            incident =>
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(TransportEquipment = Seq(transportEquipment)))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentTransportEquipments
              result.sectionTitle.value mustEqual "Transport equipment"

              result.children.size mustEqual 1

              result.children.head.sectionTitle.value mustEqual "Transport equipment 1"
          }
        }
      }
    }

    "incidentReplacementMeansOfTransport" - {
      "must return Nil" - {
        s"when replacementMeansOfTransport undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentReplacementMeansOfTransport mustEqual Nil
        }
      }

      "must return Some(Row)" - {
        s"when incident defined" in {
          forAll(arbitrary[IncidentType03], arbitrary[TranshipmentType], arbitrary[Country], arbitrary[TransportMeansIdentification]) {
            (incident, transhipment, country, identification) =>
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Transhipment = Some(transhipment)))
              )

              val answers = emptyUserAnswers
                .copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))
                .setValue(IdentificationPage(index), identification)
                .setValue(NationalityPage(index), country)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentReplacementMeansOfTransport

              result.head.key.value mustEqual "Identification type"
              result.head.value.value mustEqual identification.toString

              result(1).key.value mustEqual "Identification number"
              result(1).value.value mustEqual transhipment.TransportMeans.identificationNumber

              result(2).key.value mustEqual "Registered country"
              result(2).value.value mustEqual country.description

          }
        }
      }
    }
  }
}
