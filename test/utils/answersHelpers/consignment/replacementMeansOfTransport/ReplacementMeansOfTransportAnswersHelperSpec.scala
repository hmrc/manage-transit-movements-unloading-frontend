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

package utils.answersHelpers.consignment.replacementMeansOfTransport

import generated.{ConsignmentType05, IncidentType03, Number0, TranshipmentType}
import models.reference.{Country, TransportMeansIdentification}
import org.scalacheck.Arbitrary.arbitrary
import pages.incident.replacementMeansOfTransport.{IdentificationPage, NationalityPage}
import utils.answersHelpers.AnswersHelperSpecBase

class ReplacementMeansOfTransportAnswersHelperSpec extends AnswersHelperSpecBase {

  "ReplacementMeansOfTransportAnswersHelperSpec" - {

    "typeOfIdentification" - {
      "must return None" - {
        s"when replacementMeansOfTransport undefined" in {
          val helper = new ReplacementMeansOfTransportAnswersHelper(emptyUserAnswers, None, index)
          helper.typeOfIdentification must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when replacementMeansOfTransport defined" in {
          forAll(arbitrary[TranshipmentType], arbitrary[IncidentType03], arbitrary[TransportMeansIdentification]) {
            (transhipment, incident, identification) =>
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Transhipment = Some(transhipment)))
              )

              val answers = emptyUserAnswers
                .copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))
                .setValue(IdentificationPage(index), identification)

              val helper = new ReplacementMeansOfTransportAnswersHelper(answers, Some(transhipment), index)
              val result = helper.typeOfIdentification.value

              result.key.value mustEqual "Identification type"
              result.value.value mustEqual identification.toString

          }
        }
      }
    }

    "identificationNumber" - {
      "must return None" - {
        s"when replacementMeansOfTransport undefined" in {
          val helper = new ReplacementMeansOfTransportAnswersHelper(emptyUserAnswers, None, index)
          helper.identificationNumber must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when replacementMeansOfTransport defined" in {
          forAll(arbitrary[TranshipmentType], arbitrary[IncidentType03]) {
            (transhipment, incident) =>
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Transhipment = Some(transhipment)))
              )

              val answers = emptyUserAnswers
                .copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))

              val helper = new ReplacementMeansOfTransportAnswersHelper(answers, Some(transhipment), index)
              val result = helper.identificationNumber.value

              result.key.value mustEqual "Identification number"
              result.value.value mustEqual transhipment.TransportMeans.identificationNumber

          }
        }
      }
    }

    "nationality" - {
      "must return None" - {
        s"when replacementMeansOfTransport undefined" in {
          val helper = new ReplacementMeansOfTransportAnswersHelper(emptyUserAnswers, None, index)
          helper.nationality must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when replacementMeansOfTransport defined" in {
          forAll(arbitrary[TranshipmentType], arbitrary[IncidentType03], arbitrary[Country]) {
            (transhipment, incident, country) =>
              val consignment = ConsignmentType05(
                containerIndicator = Number0,
                Incident = Seq(incident.copy(Transhipment = Some(transhipment)))
              )

              val answers = emptyUserAnswers
                .copy(ie043Data = emptyUserAnswers.ie043Data.copy(Consignment = Some(consignment)))
                .setValue(NationalityPage(index), country)

              val helper = new ReplacementMeansOfTransportAnswersHelper(answers, Some(transhipment), index)
              val result = helper.nationality.value

              result.key.value mustEqual "Registered country"
              result.value.value mustEqual country.description

          }
        }
      }
    }

  }
}
