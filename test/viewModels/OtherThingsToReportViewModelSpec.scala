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

package viewModels

import base.SpecBase
import generators.Generators
import models.NormalMode
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.OtherThingsToReportViewModel.OtherThingsToReportViewModelProvider

class OtherThingsToReportViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when newAuth is false" in {
      val viewModelProvider = new OtherThingsToReportViewModelProvider()
      val result            = viewModelProvider.apply(arrivalId, NormalMode, newAuth = false, sealsReplaced = None)

      result.title mustBe "What do you want to report?"
      result.heading mustBe "What do you want to report?"
      result.hint must not be defined
      result.additionalHtml must not be defined
      result.requiredError mustBe "Enter what you want to report"
      result.maxLengthError mustBe "Your report must be 512 characters or less"
      result.invalidError mustBe "Your report must only include letters a to z without accents, numbers 0 to 9, ampersands (&), apostrophes, at signs (@), commas, forward slashes, full stops, hyphens, question marks and spaces"
      result.onSubmitCall.url mustBe controllers.routes.OtherThingsToReportController.onSubmit(arrivalId, NormalMode).url
    }

    "when newAuth is true and sealsReplaced is false" in {
      val viewModelProvider = new OtherThingsToReportViewModelProvider()
      val result            = viewModelProvider.apply(arrivalId, NormalMode, newAuth = true, sealsReplaced = Some(false))

      result.title mustBe "Enter all the original seal identification numbers"
      result.heading mustBe "Enter all the original seal identification numbers"
      result.hint mustBe Some("Each seal can be up to 20 characters long and include both letters and numbers.")
      result.additionalHtml must be(defined)
      result.requiredError mustBe "Enter all the original seal identification numbers"
      result.maxLengthError mustBe "The identification numbers must be 512 characters or less"
      result.invalidError mustBe "The identification numbers must only include letters a to z without accents, numbers 0 to 9, ampersands (&), apostrophes, at signs (@), commas, forward slashes, full stops, hyphens, question marks and spaces"
      result.onSubmitCall.url mustBe controllers.routes.OtherThingsToReportController.onSubmit(arrivalId, NormalMode).url
    }

    "when newAuth is true and sealsReplace is true" in {
      val viewModelProvider = new OtherThingsToReportViewModelProvider()
      val result            = viewModelProvider.apply(arrivalId, NormalMode, newAuth = true, sealsReplaced = Some(true))

      result.title mustBe "Enter all the seal identification numbers"
      result.heading mustBe "Enter all the seal identification numbers"
      result.hint mustBe Some("Each seal can be up to 20 characters long and include both letters and numbers.")
      result.additionalHtml must be(defined)
      result.requiredError mustBe "Enter all the seal identification numbers"
      result.maxLengthError mustBe "The identification numbers must be 512 characters or less"
      result.invalidError mustBe "The identification numbers must only include letters a to z without accents, numbers 0 to 9, ampersands (&), apostrophes, at signs (@), commas, forward slashes, full stops, hyphens, question marks and spaces"
      result.onSubmitCall.url mustBe controllers.routes.OtherThingsToReportController.onSubmit(arrivalId, NormalMode).url
    }
  }
}
