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

import models.CheckMode
import models.reference.{Country, TransportMeansIdentification}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import utils.answersHelpers.AnswersHelperSpecBase

class DepartureTransportMeansAnswersHelperSpec extends AnswersHelperSpecBase {

  "DepartureTransportMeansAnswersHelper" - {

    "transportMeansID" - {
      val page = TransportMeansIdentificationPage(dtmIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, dtmIndex)
          helper.transportMeansID must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[TransportMeansIdentification]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DepartureTransportMeansAnswersHelper(answers, dtmIndex)
              val result = helper.transportMeansID.value

              result.key.value mustEqual "Identification type"
              result.value.value mustEqual value.description
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.departureMeansOfTransport.routes.IdentificationController
                .onPageLoad(arrivalId, transportMeansIndex, CheckMode)
                .url
              action.visuallyHiddenText.value mustEqual "identification type for departure means of transport 1"
              action.id mustEqual "change-transport-means-identification-1"
          }
        }
      }
    }

    "transportMeansNumber" - {
      val page = VehicleIdentificationNumberPage(dtmIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, dtmIndex)
          helper.transportMeansNumber must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DepartureTransportMeansAnswersHelper(answers, dtmIndex)
              val result = helper.transportMeansNumber.value

              result.key.value mustEqual "Identification"
              result.value.value mustEqual value
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.departureMeansOfTransport.routes.IdentificationNumberController
                .onPageLoad(arrivalId, transportMeansIndex, CheckMode)
                .url
              action.visuallyHiddenText.value mustEqual "identification for departure means of transport 1"
              action.id mustEqual "change-transport-means-identification-number-1"
          }
        }
      }
    }

    "transportRegisteredCountry" - {
      val page = CountryPage(dtmIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, dtmIndex)
          helper.transportRegisteredCountry must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DepartureTransportMeansAnswersHelper(answers, dtmIndex)
              val result = helper.transportRegisteredCountry.value

              result.key.value mustEqual "Registered country"
              result.value.value mustEqual value.description
              val action = result.actions.value.items.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.departureMeansOfTransport.routes.CountryController.onPageLoad(arrivalId, transportMeansIndex, CheckMode).url
              action.visuallyHiddenText.value mustEqual "registered country for departure means of transport 1"
              action.id mustEqual "change-registered-country"
          }
        }
      }
    }
  }
}
