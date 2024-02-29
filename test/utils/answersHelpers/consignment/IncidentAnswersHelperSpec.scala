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

import generated.{AddressType18, ConsignmentType05, EndorsementType03, GNSSType, IncidentType04, LocationType02, Number0, TranshipmentType02}
import models.Coordinates
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.{Country, Incident, QualifierOfIdentification}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.incident.endorsement.EndorsementCountryPage
import pages.incident.location._
import pages.incident.transhipment.{IdentificationPage, NationalityPage}
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
          helper.incidentCountryRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentCountryRow.value

              result.key.value mustBe "Country"
              result.value.value mustBe value.toString
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentCodeRow" - {
      val page = IncidentCodePage(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentCodeRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Incident]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentCodeRow.value

              result.key.value mustBe "Incident code"
              result.value.value mustBe s"${value.toString}"
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentDescriptionRow" - {
      val page = IncidentTextPage(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentDescriptionRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentDescriptionRow.value

              result.key.value mustBe "Description"
              result.value.value mustBe s"$value"
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentQualifierRow" - {
      val page = QualifierOfIdentificationPage(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentQualifierRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[QualifierOfIdentification]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentQualifierRow.value

              result.key.value mustBe "Identifier type"
              result.value.value mustBe s"${value.toString}"
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentEndorsementDateRow" - {
      "must return None" - {
        s"when endorsement undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentDescriptionRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when endorsement defined" in {
          forAll(arbitrary[IncidentType04], arbitrary[EndorsementType03]) {
            (incident, endorsement) =>
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Endorsement = Some(endorsement)))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentEndorsementDateRow.value

              result.key.value mustBe "Endorsement date"
              result.value.value mustBe s"${endorsement.date.toGregorianCalendar.toZonedDateTime.format(cyaDateFormatter)}"
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentEndorsementAuthorityRow" - {
      "must return None" - {
        s"when endorsement undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentEndorsementAuthorityRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when endorsement defined" in {
          forAll(arbitrary[IncidentType04], arbitrary[EndorsementType03]) {
            (incident, endorsement) =>
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Endorsement = Some(endorsement)))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentEndorsementAuthorityRow.value

              result.key.value mustBe "Authority"
              result.value.value mustBe s"${endorsement.authority}"
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentEndorsementPlaceRow" - {
      "must return None" - {
        s"when endorsement undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentEndorsementPlaceRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when endorsement defined" in {
          forAll(arbitrary[IncidentType04], arbitrary[EndorsementType03]) {
            (incident, endorsement) =>
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Endorsement = Some(endorsement)))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentEndorsementPlaceRow.value

              result.key.value mustBe "Location"
              result.value.value mustBe s"${endorsement.place}"
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentEndorsementCountryRow" - {
      val page = EndorsementCountryPage(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentEndorsementCountryRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentEndorsementCountryRow.value

              result.key.value mustBe "Country"
              result.value.value mustBe s"${value.description}"
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentLocationAddressRow" - {
      "must return None" - {
        s"when location address undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentLocationAddressRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when location address defined" in {
          forAll(arbitrary[IncidentType04], arbitrary[LocationType02]) {
            (incident, location) =>
              val addressType18 = Some(AddressType18("streetAndNumber", Some("postcode"), "city"))
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(
                  incident.copy(Location =
                    LocationType02(location.qualifierOfIdentification, location.UNLocode, location.country, location.GNSS, addressType18)
                  )
                )
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentLocationAddressRow.value

              result.key.value mustBe "Address"
              result.value.value mustBe s"${addressType18.value.streetAndNumber}<br>${addressType18.value.city}<br>${addressType18.value.postcode.get}"
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentCoordinatesRow" - {
      "must return None" - {
        s"when incident undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentCoordinatesRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when incident defined" in {
          forAll(arbitrary[IncidentType04], arbitrary[Coordinates], arbitrary[LocationType02]) {
            (incident, coordinate, locationType02) =>
              val locationType = locationType02.copy(GNSS = Some(GNSSType(coordinate.latitude, coordinate.longitude)))
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Location = locationType))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentCoordinatesRow.value

              result.key.value mustBe "Coordinates"
              result.value.value mustBe s"$coordinate"
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentUnLocodeRow" - {
      "must return None" - {
        s"when incident undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentUnLocodeRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when incident defined" in {
          forAll(arbitrary[IncidentType04], Gen.alphaNumStr, arbitrary[LocationType02]) {
            (incident, unLocode, locationType02) =>
              val locationType = locationType02.copy(UNLocode = Some(unLocode))
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Location = locationType))
              )

              val answers = emptyUserAnswers.copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentUnLocodeRow.value

              result.key.value mustBe "UN/LOCODE"
              result.value.value mustBe unLocode
              val action = result.actions
              action mustBe None
          }
        }
      }
    }

    "incidentTranshipment" - {
      "must return Nil" - {
        s"when transhipment undefined" in {
          val helper = new IncidentAnswersHelper(emptyUserAnswers, index)
          helper.incidentTranshipment mustBe Nil
        }
      }

      "must return Some(Row)" - {
        s"when incident defined" in {
          forAll(arbitrary[IncidentType04], arbitrary[TranshipmentType02], arbitrary[Country], arbitrary[TransportMeansIdentification]) {
            (incident, transhipment, country, identification) =>
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Transhipment = Some(transhipment)))
              )

              val answers = emptyUserAnswers
                .copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))
                .setValue(IdentificationPage(index), identification)
                .setValue(NationalityPage(index), country)

              val helper = new IncidentAnswersHelper(answers, index)
              val result = helper.incidentTranshipment

              result.head.key.value mustBe "Container indicator"
              result.head.value.value mustBe transhipment.containerIndicator.toString

              result(1).key.value mustBe "Identification type"
              result(1).value.value mustBe identification.toString

              result(2).key.value mustBe "Identification number"
              result(2).value.value mustBe transhipment.TransportMeans.identificationNumber

              result(3).key.value mustBe "Registered country"
              result(3).value.value mustBe country.description

          }
        }
      }
    }
  }
}
