/*
 * Copyright 2023 HM Revenue & Customs
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

package viewModels.additionalReference.index

import base.SpecBase
import generators.Generators
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.AdditionalReferenceNumberPage
import viewModels.additionalReference.index.AddAnotherAdditionalReferenceViewModel.AddAnotherAdditionalReferenceViewModelProvider

class AddAnotherAdditionalReferenceViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is no additional references" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(emptyUserAnswers, arrivalId, mode)

          result.listItems mustBe Nil

          result.title mustBe s"You have added 0 additional references"
          result.heading mustBe s"You have added 0 additional references"
          result.legend mustBe s"Do you want to add a additional reference?"
          result.maxLimitLabel mustBe
            s"You cannot add any more additional references. To add another, you need to remove one first."
      }
    }

    "when there is one additional reference" in {
      forAll(arbitrary[Mode], nonEmptyString) {
        (mode, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), identificationNumber)

          val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(userAnswers, arrivalId, mode)

          result.listItems.length mustBe 1
          result.title mustBe s"You have added 1 additional reference"
          result.heading mustBe s"You have added 1 additional reference"
          result.legend mustBe s"Do you want to add another additional reference?"
          result.maxLimitLabel mustBe
            s"You cannot add any more additional references. To add another, you need to remove one first."
      }
    }

    "when there are multiple additional references" in {

      forAll(arbitrary[Mode], nonEmptyString) {
        (mode, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(AdditionalReferenceNumberPage(Index(0)), identificationNumber)
            .setValue(AdditionalReferenceNumberPage(Index(1)), identificationNumber)
            .setValue(AdditionalReferenceNumberPage(Index(2)), identificationNumber)
            .setValue(AdditionalReferenceNumberPage(Index(3)), identificationNumber)

          val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(userAnswers, arrivalId, mode)
          result.listItems.length mustBe 4
          result.title mustBe s"You have added 4 additional references"
          result.heading mustBe s"You have added 4 additional references"
          result.legend mustBe s"Do you want to add another additional reference?"
          result.maxLimitLabel mustBe
            s"You cannot add any more additional references. To add another, you need to remove one first."
      }
    }
  }
}
