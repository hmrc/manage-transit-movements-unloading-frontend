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

package viewModels.houseConsignment.index.additionalReference

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.AdditionalReferenceType
import models.{Index, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.additionalReference.{HouseConsignmentAdditionalReferenceNumberPage, HouseConsignmentAdditionalReferenceTypePage}
import viewModels.houseConsignment.index.additionalReference.AddAnotherAdditionalReferenceViewModel.AddAnotherAdditionalReferenceViewModelProvider

class AddAnotherAdditionalReferenceTypeViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is no additional references" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(emptyUserAnswers, arrivalId, mode, houseConsignmentIndex)

          result.listItems mustEqual Nil

          result.title mustEqual s"You have added 0 additional references for house consignment 1"
          result.heading mustEqual s"You have added 0 additional references for house consignment 1"
          result.legend mustEqual s"Do you want to add a additional reference for house consignment 1?"
          result.maxLimitLabel mustEqual
            s"You cannot add any more additional references for house consignment 1. To add another, you need to remove one first."
      }
    }

    "when there is one additional reference" in {
      forAll(arbitrary[Mode], arbitraryAdditionalReference.arbitrary.sample.value) {
        (mode, identificationReference) =>
          val userAnswers = emptyUserAnswers
            .setValue(HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, additionalReferenceIndex), identificationReference)

          val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(userAnswers, arrivalId, mode, houseConsignmentIndex)

          result.listItems.length mustEqual 1
          result.title mustEqual s"You have added 1 additional reference for house consignment 1"
          result.heading mustEqual s"You have added 1 additional reference for house consignment 1"
          result.legend mustEqual s"Do you want to add another additional reference for house consignment 1?"
          result.maxLimitLabel mustEqual
            s"You cannot add any more additional references for house consignment 1. To add another, you need to remove one first."
      }
    }

    "when there are multiple additional references" in {

      forAll(arbitrary[Mode], arbitraryAdditionalReference.arbitrary.sample.value) {
        (mode, identificationReference) =>
          val userAnswers = emptyUserAnswers
            .setValue(HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, Index(0)), identificationReference)
            .setValue(HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, Index(1)), identificationReference)
            .setValue(HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, Index(2)), identificationReference)
            .setValue(HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, Index(3)), identificationReference)

          val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(userAnswers, arrivalId, mode, houseConsignmentIndex)
          result.listItems.length mustEqual 4
          result.title mustEqual s"You have added 4 additional references for house consignment 1"
          result.heading mustEqual s"You have added 4 additional references for house consignment 1"
          result.legend mustEqual s"Do you want to add another additional reference for house consignment 1?"
          result.maxLimitLabel mustEqual
            s"You cannot add any more additional references for house consignment 1. To add another, you need to remove one first."
      }
    }

    "when there is one additional reference and it displays code and description with additional reference number" in {
      val userAnswers = emptyUserAnswers
        .setValue(HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, Index(0)), AdditionalReferenceType("code", "description"))
        .setValue(HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, Index(0)), "additionalReferenceNumber")
      val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(userAnswers, arrivalId, NormalMode, houseConsignmentIndex)
      result.listItems.length mustEqual 1
      result.listItems.head.name mustEqual "code - description - additionalReferenceNumber"
    }

    "when there is one additional reference and it displays code and description without additional reference number" in {
      val userAnswers = emptyUserAnswers
        .setValue(HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, Index(0)), AdditionalReferenceType("code", "description"))
      val result = new AddAnotherAdditionalReferenceViewModelProvider().apply(userAnswers, arrivalId, NormalMode, houseConsignmentIndex)
      result.listItems.length mustEqual 1
      result.listItems.head.name mustEqual "code - description"
    }
  }
}
