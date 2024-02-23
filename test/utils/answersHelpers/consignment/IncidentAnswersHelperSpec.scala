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

import models.reference.Incident
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.incident.{IncidentCodePage, IncidentTextPage}
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

  }
}
