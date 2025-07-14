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

package viewModels.documents

import base.SpecBase
import generators.Generators
import models.{CheckMode, ConsignmentLevelDocuments, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.documents.TypeViewModel.TypeViewModelProvider

class TypeViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when Normal mode" in {
      val viewModelProvider = new TypeViewModelProvider()
      val result            = viewModelProvider.apply(NormalMode, ConsignmentLevelDocuments(0, 0))

      result.title mustEqual "What type of document do you want to add?"
      result.heading mustEqual "What type of document do you want to add?"
    }

    "when Check mode" in {
      val viewModelProvider = new TypeViewModelProvider()
      val result            = viewModelProvider.apply(CheckMode, ConsignmentLevelDocuments(0, 0))

      result.title mustEqual "What is the new document type?"
      result.heading mustEqual "What is the new document type?"
    }
  }

  "when transport documents reached max limit" in {
    forAll(arbitrary[Mode]) {
      mode =>
        val result = new TypeViewModelProvider().apply(mode, ConsignmentLevelDocuments(0, frontendAppConfig.maxTransportDocumentsConsignment))

        result.maxLimitLabelForType.get mustEqual "You cannot add any more transport documents to all items. To add another, you need to remove one first. You can, however, still add a supporting document to your items."
    }
  }

  "when supporting documents reached max limit" in {
    forAll(arbitrary[Mode]) {
      mode =>
        val result = new TypeViewModelProvider().apply(mode, ConsignmentLevelDocuments(frontendAppConfig.maxSupportingDocumentsConsignment, 0))

        result.maxLimitLabelForType.get mustEqual "You cannot add any more supporting documents to all items. To add another, you need to remove one first. You can, however, still add a transport document to your items."
    }
  }
}
