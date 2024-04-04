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

package utils.answersHelpers.consignment.houseConsignment

import models.reference.{Country, TransportMeansIdentification}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages._
import utils.answersHelpers.AnswersHelperSpecBase

class DepartureTransportMeansAnswersHelperSpec extends AnswersHelperSpecBase {

  "DepartureTransportMeansAnswersHelper" - {

    "transportMeansID" - {
      val page = DepartureTransportMeansIdentificationTypePage(hcIndex, dtmIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, hcIndex, dtmIndex)
          helper.transportMeansID mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[TransportMeansIdentification]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DepartureTransportMeansAnswersHelper(answers, hcIndex, dtmIndex)
              val result = helper.transportMeansID.value

              result.key.value mustBe "Identification type"
              result.value.value mustBe value.description
              result.actions must not be defined
          }
        }
      }
    }

    "transportMeansIDNumber" - {
      val page = DepartureTransportMeansIdentificationNumberPage(hcIndex, dtmIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, hcIndex, dtmIndex)
          helper.transportMeansIDNumber mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DepartureTransportMeansAnswersHelper(answers, hcIndex, dtmIndex)
              val result = helper.transportMeansIDNumber.value

              result.key.value mustBe "Identification number"
              result.value.value mustBe value
              result.actions must not be defined
          }
        }
      }
    }

    "buildVehicleNationalityRow" - {
      val page = DepartureTransportMeansCountryPage(hcIndex, dtmIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, hcIndex, dtmIndex)
          helper.buildVehicleNationalityRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[Country]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new DepartureTransportMeansAnswersHelper(answers, hcIndex, dtmIndex)
              val result = helper.buildVehicleNationalityRow.value

              result.key.value mustBe "Registered country"
              result.value.value mustBe value.description
              result.actions must not be defined
          }
        }
      }
    }
  }
}
