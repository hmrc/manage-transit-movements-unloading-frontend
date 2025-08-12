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

package viewModels.houseConsignment.index.items.additionalReference

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.AdditionalReferenceType
import models.{Index, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.additionalReference.AdditionalReferenceTypePage
import pages.houseConsignment.index.items.additionalReference.AdditionalReferenceNumberPage
import viewModels.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferenceViewModel.AddAnotherAdditionalReferenceViewModelProvider

class AddAnotherAdditionalReferenceTypeViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is no additional references" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(emptyUserAnswers, arrivalId, mode, mode, houseConsignmentIndex, itemIndex)

          result.listItems mustEqual Nil

          result.title mustEqual s"You have added 0 additional references for item 1 in house consignment 1"
          result.heading mustEqual s"You have added 0 additional references for item 1 in house consignment 1"
          result.legend mustEqual s"Do you want to add a additional reference for item 1 in house consignment 1?"
          result.maxLimitLabel mustEqual
            s"You cannot add any more additional references for item 1 in house consignment 1. To add another, you need to remove one first."
      }
    }

    "when there is one additional reference" in {
      forAll(arbitrary[Mode], arbitraryAdditionalReference.arbitrary.sample.value) {
        (mode, identificationReference) =>
          val userAnswers = emptyUserAnswers
            .setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), identificationReference)

          val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(userAnswers, arrivalId, mode, mode, houseConsignmentIndex, itemIndex)

          result.listItems.length mustEqual 1
          result.title mustEqual s"You have added 1 additional reference for item 1 in house consignment 1"
          result.heading mustEqual s"You have added 1 additional reference for item 1 in house consignment 1"
          result.legend mustEqual s"Do you want to add another additional reference for item 1 in house consignment 1?"
          result.maxLimitLabel mustEqual
            s"You cannot add any more additional references for item 1 in house consignment 1. To add another, you need to remove one first."
      }
    }

    "when there are multiple additional references" in {

      forAll(arbitrary[Mode], arbitraryAdditionalReference.arbitrary.sample.value) {
        (mode, identificationReference) =>
          val userAnswers = emptyUserAnswers
            .setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, Index(0)), identificationReference)
            .setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, Index(1)), identificationReference)
            .setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, Index(2)), identificationReference)
            .setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, Index(3)), identificationReference)

          val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(userAnswers, arrivalId, mode, mode, houseConsignmentIndex, itemIndex)
          result.listItems.length mustEqual 4
          result.title mustEqual s"You have added 4 additional references for item 1 in house consignment 1"
          result.heading mustEqual s"You have added 4 additional references for item 1 in house consignment 1"
          result.legend mustEqual s"Do you want to add another additional reference for item 1 in house consignment 1?"
          result.maxLimitLabel mustEqual
            s"You cannot add any more additional references for item 1 in house consignment 1. To add another, you need to remove one first."
      }
    }

    "when there is one additional reference and it displays code and description with additional reference number at HouseConsignment Item Level" in {
      val userAnswers = emptyUserAnswers
        .setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, Index(0)), AdditionalReferenceType("code", "description"))
        .setValue(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, Index(0)), "additionalReferenceNumber")
      val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(userAnswers, arrivalId, NormalMode, NormalMode, houseConsignmentIndex, itemIndex)
      result.listItems.length mustEqual 1
      result.listItems.head.name mustEqual "code - description - additionalReferenceNumber"
    }

    "when there is one additional reference and it displays code and description without additional reference number at HouseConsignment Item Level" in {
      val userAnswers = emptyUserAnswers
        .setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, Index(0)), AdditionalReferenceType("code", "description"))
      val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(userAnswers, arrivalId, NormalMode, NormalMode, houseConsignmentIndex, itemIndex)
      result.listItems.length mustEqual 1
      result.listItems.head.name mustEqual "code - description"
    }

  }
}
