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

import base.AppWithDefaultMockFixtures
import models.reference.AdditionalInformationCode
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.additionalinformation.{HouseConsignmentAdditionalInformationCodePage, HouseConsignmentAdditionalInformationTextPage}
import utils.answersHelpers.AnswersHelperSpecBase

class HouseConsignmentAdditionalInformationAnswersHelperSpec extends AnswersHelperSpecBase {

  "AdditionalInformationHelper" - {

    "codeRow" - {
      val page = HouseConsignmentAdditionalInformationCodePage(hcIndex, additionalInformationIndex)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAdditionalInformationAnswersHelper(emptyUserAnswers, hcIndex, additionalInformationIndex)
          helper.code must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[AdditionalInformationCode]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAdditionalInformationAnswersHelper(answers, hcIndex, additionalInformationIndex)
              val result = helper.code.value

              result.key.value mustEqual "Type"
              result.value.value mustEqual value.toString
              result.actions must not be defined
          }
        }
      }
    }

    "descriptionRow" - {
      val page = HouseConsignmentAdditionalInformationTextPage(hcIndex, additionalInformationIndex)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new HouseConsignmentAdditionalInformationAnswersHelper(emptyUserAnswers, hcIndex, additionalInformationIndex)
          helper.description must not be defined
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(nonEmptyString) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new HouseConsignmentAdditionalInformationAnswersHelper(answers, hcIndex, additionalInformationIndex)
              val result = helper.description.value

              result.key.value mustEqual "Description"
              result.value.value mustEqual value
              result.actions must not be defined
          }
        }
      }
    }
  }
}
