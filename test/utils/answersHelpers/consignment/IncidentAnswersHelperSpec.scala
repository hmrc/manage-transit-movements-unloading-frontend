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

import generated.{ConsignmentType05, EndorsementType03, GNSSType, IncidentType04, LocationType02, Number0}
import models.Coordinates
import models.reference.{Country, Incident}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.incident.endorsement.EndorsementCountryPage
import pages.incident.{IncidentCodePage, IncidentTextPage}
import utils.Format.cyaDateFormatter
import utils.answersHelpers.AnswersHelperSpecBase

class IncidentAnswersHelperSpec extends AnswersHelperSpecBase {

  "IncidentAnswersHelper" - {

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

    "incidentTextRow" - {
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

  }
}
