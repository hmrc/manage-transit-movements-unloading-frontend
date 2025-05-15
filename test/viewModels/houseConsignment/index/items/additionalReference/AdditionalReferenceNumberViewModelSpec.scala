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

package viewModels.houseConsignment.index.items.additionalReference

import base.SpecBase
import generators.Generators
import models.{CheckMode, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.houseConsignment.index.items.additionalReference.AdditionalReferenceNumberViewModel.AdditionalReferenceNumberViewModelProvider

class AdditionalReferenceNumberViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val houseConsignmentMode = arbitrary[Mode].sample.value
  private val itemMode             = arbitrary[Mode].sample.value

  "must create view model" - {
    "when Normal mode" in {
      val viewModelProvider = new AdditionalReferenceNumberViewModelProvider()
      val result = viewModelProvider.apply(arrivalId, houseConsignmentMode, itemMode, NormalMode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)

      result.title mustBe s"What is the additional reference number for item ${itemIndex.display} in house consignment ${houseConsignmentIndex.display}?"
      result.heading mustBe s"What is the additional reference number for item ${itemIndex.display} in house consignment ${houseConsignmentIndex.display}?"
    }

    "when Check mode" in {
      val viewModelProvider = new AdditionalReferenceNumberViewModelProvider()

      val result = viewModelProvider.apply(arrivalId, houseConsignmentMode, itemMode, CheckMode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)

      result.title mustBe s"What is the new additional reference number for item ${itemIndex.display} in house consignment ${houseConsignmentIndex.display}?"
      result.heading mustBe s"What is the new additional reference number for item ${itemIndex.display} in house consignment ${houseConsignmentIndex.display}?"
    }
  }
}
