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

import models.reference.{AdditionalInformationCode, AdditionalReferenceType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.additionalInformation.{AdditionalInformationCodePage, AdditionalInformationTextPage}
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.sections.additionalInformation.AdditionalInformationSection
import pages.sections.additionalInformation.AdditionalInformationSection.AdditionalInformation
import pages.sections.additionalReference.AdditionalReferenceSection
import pages.sections.additionalReference.AdditionalReferenceSection.AdditionalReference
import utils.answersHelpers.AnswersHelperSpecBase

class AdditionalInformationAnswersHelperSpec extends AnswersHelperSpecBase {

  "AdditionalInformationAnswersHelperSpec" - {

    "additionalInformation" - {
      val page = AdditionalInformationSection(index)
      "must return None" - {
        s"when $page undefined" in {
          val helper = new AdditionalInformationAnswersHelper(emptyUserAnswers, index)
          helper.additionalInformation mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $page defined" in {
          forAll(arbitrary[AdditionalInformationCode], Gen.option(Gen.alphaNumStr)) {
            (code, text) =>
              val value = AdditionalInformation(code, text)

              val answers = emptyUserAnswers
                .setValue(AdditionalInformationCodePage(index), code)
                .setValue(AdditionalInformationTextPage(index), text)

              val helper = new AdditionalInformationAnswersHelper(answers, index)
              val result = helper.additionalInformation.value

              result.key.value mustBe "Additional information 1"
              result.value.value mustBe value.toString
              result.actions mustBe None
          }
        }
      }
    }
  }
}
