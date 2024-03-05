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

package utils.answersHelpers.consignment.houseConsignment.item

import models.reference.AdditionalInformationCode
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.items.additionalinformation.HouseConsignmentAdditionalInformationCodePage
import utils.answersHelpers.AnswersHelperSpecBase

class AdditionalInformationsAnswersHelperSpec extends AnswersHelperSpecBase {

  "AdditionalInformationsHelper" - {

    "additionalInformationRow" - {
      val page = HouseConsignmentAdditionalInformationCodePage(hcIndex, itemIndex, additionalInformationIndex)

      "must return None" - {
        s"when $page undefined" in {
          val helper = new AdditionalInformationsAnswerHelper(emptyUserAnswers, hcIndex, itemIndex, additionalReferenceIndex)
          helper.additionalInformationRow mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[AdditionalInformationCode]) {
            value =>
              val answers = emptyUserAnswers.setValue(page, value)

              val helper = new AdditionalInformationsAnswerHelper(answers, hcIndex, itemIndex, additionalReferenceIndex)
              val result = helper.additionalInformationRow.value

              result.key.value mustBe "Additional Information 1"
              result.value.value mustBe value.toString
              result.actions.head.items.head.href mustBe "#"
          }
        }
      }
    }
  }
}
