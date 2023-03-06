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

package viewModels

import base.SpecBase
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import pages._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import java.time.LocalDate

class UnloadingRemarksRejectionViewModelSpec extends SpecBase with Generators {

  "apply" - {

    "must return None" - {
      "when user answers empty" in {
        val viewModel = new UnloadingRemarksRejectionViewModel()
        val result    = viewModel.apply(emptyUserAnswers)
        result mustBe None
      }
    }

    "must return summary list row" - {
      "when pointer is NumberOfPackagesPointer" in {
        val value       = arbitrary[Int].sample.value
        val viewModel   = new UnloadingRemarksRejectionViewModel()
        val userAnswers = emptyUserAnswers.setValue(TotalNumberOfPackagesPage, value.toString)
        val result      = viewModel.apply(userAnswers).get
        result.value mustBe Value(value.toString.toText)
      }

      "when pointer is VehicleRegistrationPointer" in {
        val value       = arbitrary[String].sample.value
        val viewModel   = new UnloadingRemarksRejectionViewModel()
        val userAnswers = emptyUserAnswers.setValue(VehicleIdentificationNumberPage, value)
        val result      = viewModel.apply(userAnswers).get
        result.value mustBe Value(value.toText)
      }

      "when pointer is NumberOfItemsPointer" in {
        val value       = arbitrary[Int].sample.value
        val viewModel   = new UnloadingRemarksRejectionViewModel()
        val userAnswers = emptyUserAnswers.setValue(TotalNumberOfItemsPage, value)
        val result      = viewModel.apply(userAnswers).get
        result.value mustBe Value(value.toString.toText)
      }

      "when pointer is GrossWeightPointer" in {
        val value       = arbitrary[String].sample.value
        val viewModel   = new UnloadingRemarksRejectionViewModel()
        val userAnswers = emptyUserAnswers.setValue(GrossWeightPage, value)
        val result      = viewModel.apply(userAnswers).get
        result.value mustBe Value(value.toText)
      }

      "when pointer is UnloadingDatePointer" in {
        val value          = LocalDate.parse("2000-01-01")
        val formattedValue = "1 January 2000"
        val viewModel      = new UnloadingRemarksRejectionViewModel()
        val userAnswers    = emptyUserAnswers.setValue(DateGoodsUnloadedPage, value)
        val result         = viewModel.apply(userAnswers).get
        result.value mustBe Value(formattedValue.toText)
      }
    }
  }
}
