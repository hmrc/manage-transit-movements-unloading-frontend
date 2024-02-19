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

package utils.answersHelpers

import models.reference.AdditionalReferenceType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.sections.additionalReference.AdditionalReferenceSection
import pages.sections.additionalReference.AdditionalReferenceSection.AdditionalReference

class AdditionalReferenceAnswersHelperSpec extends AnswersHelperSpecBase {

  "AdditionalReferenceAnswersHelper" - {

    "additionalReference" - {
      val page = AdditionalReferenceSection(additionalReferenceIndex)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new AdditionalReferenceAnswersHelper(emptyUserAnswers, additionalReferenceIndex)
          helper.additionalReference mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[AdditionalReferenceType], Gen.option(Gen.alphaNumStr)) {
            (`type`, number) =>
              val value = AdditionalReference(`type`, number)

              val answers = emptyUserAnswers
                .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), `type`)
                .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), number)

              val helper = new AdditionalReferenceAnswersHelper(answers, additionalReferenceIndex)
              val result = helper.additionalReference.value

              result.key.value mustBe "Additional Reference 1"
              result.value.value mustBe value.toString
              val action = result.actions.value.items.head
              action.content.value mustBe "Change"
              action.href mustBe "#"
              action.visuallyHiddenText.value mustBe "additional reference 1"
              action.id mustBe "change-additional-reference-1"
          }
        }
      }
    }
  }
}
