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

package utils.answersHelpers.consignment.transhipment

import generated.{ConsignmentType05, IncidentType04, Number0, TranshipmentType02}
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import pages.incident.transhipment.{IdentificationPage, NationalityPage}
import utils.answersHelpers.AnswersHelperSpecBase

class TranshipmentAnswersHelperSpec extends AnswersHelperSpecBase {

  "TranshipmentAnswersHelperSpec" - {

    "containerIndicator" - {
      "must return None" - {
        s"when transhipment undefined" in {
          val helper = new TranshipmentAnswersHelper(emptyUserAnswers, None, index)
          helper.containerIndicator mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when transhipment defined" in {
          forAll(arbitrary[TranshipmentType02], arbitrary[IncidentType04]) {
            (transhipment, incident) =>
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Transhipment = Some(transhipment)))
              )

              val answers = emptyUserAnswers
                .copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new TranshipmentAnswersHelper(answers, Some(transhipment), index)
              val result = helper.containerIndicator.value

              result.key.value mustBe "Container indicator"
              result.value.value mustBe transhipment.containerIndicator.toString

          }
        }
      }
    }

    "typeOfIdentification" - {
      "must return None" - {
        s"when transhipment undefined" in {
          val helper = new TranshipmentAnswersHelper(emptyUserAnswers, None, index)
          helper.typeOfIdentification mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when transhipment defined" in {
          forAll(arbitrary[TranshipmentType02], arbitrary[IncidentType04], arbitrary[TransportMeansIdentification]) {
            (transhipment, incident, identification) =>
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Transhipment = Some(transhipment)))
              )

              val answers = emptyUserAnswers
                .copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))
                .setValue(IdentificationPage(index), identification)

              val helper = new TranshipmentAnswersHelper(answers, Some(transhipment), index)
              val result = helper.typeOfIdentification.value

              result.key.value mustBe "Identification type"
              result.value.value mustBe identification.toString

          }
        }
      }
    }

    "identificationNumber" - {
      "must return None" - {
        s"when transhipment undefined" in {
          val helper = new TranshipmentAnswersHelper(emptyUserAnswers, None, index)
          helper.identificationNumber mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when transhipment defined" in {
          forAll(arbitrary[TranshipmentType02], arbitrary[IncidentType04]) {
            (transhipment, incident) =>
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Transhipment = Some(transhipment)))
              )

              val answers = emptyUserAnswers
                .copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new TranshipmentAnswersHelper(answers, Some(transhipment), index)
              val result = helper.identificationNumber.value

              result.key.value mustBe "Identification number"
              result.value.value mustBe transhipment.TransportMeans.identificationNumber

          }
        }
      }
    }

    "nationality" - {
      "must return None" - {
        s"when transhipment undefined" in {
          val helper = new TranshipmentAnswersHelper(emptyUserAnswers, None, index)
          helper.nationality mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when transhipment defined" in {
          forAll(arbitrary[TranshipmentType02], arbitrary[IncidentType04], arbitrary[Country]) {
            (transhipment, incident, country) =>
              val consignment: ConsignmentType05 = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Transhipment = Some(transhipment)))
              )

              val answers = emptyUserAnswers
                .copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))
                .setValue(NationalityPage(index), country)

              val helper = new TranshipmentAnswersHelper(answers, Some(transhipment), index)
              val result = helper.nationality.value

              result.key.value mustBe "Registered country"
              result.value.value mustBe country.description

          }
        }
      }
    }

  }
}
