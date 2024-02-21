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

package viewModels.additionalReference.index

import base.SpecBase
import generators.Generators
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.additionalReference.index.AdditionalReferenceNumberViewModel.AdditionalReferenceNumberViewModelProvider

class AdditionalReferenceNumberViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when Normal mode" in {
      val viewModelProvider = new AdditionalReferenceNumberViewModelProvider()
      val result            = viewModelProvider.apply(NormalMode)

      result.title mustBe "What is the additional reference number?"
      result.heading mustBe "What is the additional reference number?"
    }

    "when Check mode" in {
      val viewModelProvider = new AdditionalReferenceNumberViewModelProvider()

      val result = viewModelProvider.apply(CheckMode)

      result.title mustBe "What is the new additional reference number?"
      result.heading mustBe "What is the new additional reference number?"
    }
  }
}
