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

package viewModels.houseConsignment.index.documents

import base.SpecBase
import generators.Generators
import models.{CheckMode, HouseConsignmentLevelDocuments, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.houseConsignment.index.documents.TypeViewModel.TypeViewModelProvider

class TypeViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when Normal mode" in {
      val viewModelProvider = new TypeViewModelProvider()
      val result            = viewModelProvider.apply(NormalMode, HouseConsignmentLevelDocuments(0, 0), houseConsignmentIndex)

      result.title mustBe s"What type of document do you want to add for house consignment ${houseConsignmentIndex.display}?"
      result.heading mustBe s"What type of document do you want to add for house consignment ${houseConsignmentIndex.display}?"
    }

    "when Check mode" in {
      val viewModelProvider = new TypeViewModelProvider()
      val result            = viewModelProvider.apply(CheckMode, HouseConsignmentLevelDocuments(0, 0), houseConsignmentIndex)

      result.title mustBe s"What is the new document type for house consignment ${houseConsignmentIndex.display}?"
      result.heading mustBe s"What is the new document type for house consignment ${houseConsignmentIndex.display}?"
    }
  }

  "when transport documents reached max limit" in {
    forAll(arbitrary[Mode]) {
      mode =>
        val result = new TypeViewModelProvider().apply(mode,
                                                       HouseConsignmentLevelDocuments(0, frontendAppConfig.maxTransportDocumentsHouseConsignment),
                                                       houseConsignmentIndex
        )

        result.maxLimitLabelForType.get mustBe "You cannot add any more documents of this type. To add another, you need to remove one first"
    }
  }

  "when supporting documents reached max limit" in {
    forAll(arbitrary[Mode]) {
      mode =>
        val result = new TypeViewModelProvider().apply(mode,
                                                       HouseConsignmentLevelDocuments(frontendAppConfig.maxSupportingDocumentsHouseConsignment, 0),
                                                       houseConsignmentIndex
        )

        result.maxLimitLabelForType.get mustBe "You cannot add any more documents of this type. To add another, you need to remove one first"
    }
  }
}
