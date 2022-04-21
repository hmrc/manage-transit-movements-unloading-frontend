/*
 * Copyright 2022 HM Revenue & Customs
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

package viewModels

import base.SpecBase
import generators.MessagesModelGenerators
import models._
import org.scalacheck.Arbitrary.arbitrary
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class UnloadingRemarksRejectionViewModelSpec extends SpecBase with MessagesModelGenerators {

  private lazy val errorType = arbitrary[ErrorType].sample.value

  "apply" - {

    "must return None" - {
      "when originalAttributeValue is None" in {
        val pointer   = arbitraryNonDefaultErrorPointer.arbitrary.sample.value
        val error     = FunctionalError(errorType, pointer, None, None)
        val viewModel = new UnloadingRemarksRejectionViewModel()
        val result    = viewModel.apply(error, arrivalId)
        result mustBe None
      }

      "when originalAttributeValue is defined but pointer is DefaultPointer" in {
        val pointer                = arbitraryDefaultPointer.arbitrary.sample.value
        val originalAttributeValue = arbitrary[String].sample.value
        val error                  = FunctionalError(errorType, pointer, None, Some(originalAttributeValue))
        val viewModel              = new UnloadingRemarksRejectionViewModel()
        val result                 = viewModel.apply(error, arrivalId)
        result mustBe None
      }
    }

    "must return summary list row" - {
      "when pointer is NumberOfPackagesPointer" in {
        val originalAttributeValue = arbitrary[String].sample.value
        val error                  = FunctionalError(errorType, NumberOfPackagesPointer, None, Some(originalAttributeValue))
        val viewModel              = new UnloadingRemarksRejectionViewModel()
        val result                 = viewModel.apply(error, arrivalId).get
        result.value mustBe Value(originalAttributeValue.toText)
      }

      "when pointer is VehicleRegistrationPointer" in {
        val originalAttributeValue = arbitrary[String].sample.value
        val error                  = FunctionalError(errorType, VehicleRegistrationPointer, None, Some(originalAttributeValue))
        val viewModel              = new UnloadingRemarksRejectionViewModel()
        val result                 = viewModel.apply(error, arrivalId).get
        result.value mustBe Value(originalAttributeValue.toText)
      }

      "when pointer is NumberOfItemsPointer" in {
        val originalAttributeValue = arbitrary[String].sample.value
        val error                  = FunctionalError(errorType, NumberOfItemsPointer, None, Some(originalAttributeValue))
        val viewModel              = new UnloadingRemarksRejectionViewModel()
        val result                 = viewModel.apply(error, arrivalId).get
        result.value mustBe Value(originalAttributeValue.toText)
      }

      "when pointer is GrossMassPointer" in {
        val originalAttributeValue = arbitrary[String].sample.value
        val error                  = FunctionalError(errorType, GrossMassPointer, None, Some(originalAttributeValue))
        val viewModel              = new UnloadingRemarksRejectionViewModel()
        val result                 = viewModel.apply(error, arrivalId).get
        result.value mustBe Value(originalAttributeValue.toText)
      }

      "when pointer is UnloadingDatePointer" in {
        val originalAttributeValue          = "20000101"
        val formattedOriginalAttributeValue = "1 January 2000"

        val error     = FunctionalError(errorType, UnloadingDatePointer, None, Some(originalAttributeValue))
        val viewModel = new UnloadingRemarksRejectionViewModel()
        val result    = viewModel.apply(error, arrivalId).get
        result.value mustBe Value(formattedOriginalAttributeValue.toText)
      }
    }
  }
}
