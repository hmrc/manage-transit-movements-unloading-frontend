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
import models.{Mode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.OtherThingsToReportViewModel.{AdditionalHtml, OtherThingsToReportViewModelProvider}

class OtherThingsToReportViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mode: Mode = NormalMode

  "must create view model" - {
    "when newAuth is false" in {
      val viewModelProvider = new OtherThingsToReportViewModelProvider()
      val result            = viewModelProvider.apply(arrivalId, mode, newAuth = false, sealsReplaced = None)

      result.title mustBe "What do you want to report?"
      result.heading mustBe "What do you want to report?"
      result.additionalHtml mustBe None
      result.requiredError mustBe "Enter what you want to report"
      result.maxLengthError mustBe "Your report must be 512 characters or less"
      result.invalidError mustBe "Your report must only include letters a to z without accents, numbers 0 to 9, ampersands (&), apostrophes, at signs (@), commas, forward slashes, full stops, hyphens, question marks and spaces"
      result.onSubmitCall.url mustBe controllers.routes.OtherThingsToReportController.onSubmit(arrivalId, mode).url
    }

    "when newAuth is true and sealsReplaced is false" in {
      val viewModelProvider = new OtherThingsToReportViewModelProvider()
      val result            = viewModelProvider.apply(arrivalId, mode, newAuth = true, sealsReplaced = Some(false))

      result.title mustBe "What is the identification number for the external seal?"
      result.heading mustBe "What is the identification number for the external seal?"
      result.additionalHtml mustBe Some(
        AdditionalHtml(
          paragraph1 = "Only enter an original seal affixed by an authorised consignor.",
          paragraph2 = "If this seal is broken, you must",
          linkText = "select no to using the revised unloading procedure",
          linkHref = controllers.routes.NewAuthYesNoController.onPageLoad(arrivalId, mode),
          paragraph3 = "You will then need to unload the goods and report any discrepancies."
        )
      )
      result.requiredError mustBe "Enter the identification number for the external seal"
      result.maxLengthError mustBe "The identification number must be 512 characters or less"
      result.invalidError mustBe "The identification number must only include letters a to z without accents, numbers 0 to 9, ampersands (&), apostrophes, at signs (@), commas, forward slashes, full stops, hyphens, question marks and spaces"
      result.onSubmitCall.url mustBe controllers.routes.OtherThingsToReportController.onSubmit(arrivalId, mode).url
    }

    "when newAuth is true and sealsReplace is true" in {
      val viewModelProvider = new OtherThingsToReportViewModelProvider()
      val result            = viewModelProvider.apply(arrivalId, mode, newAuth = true, sealsReplaced = Some(true))

      result.title mustBe "What is the identification number for the replacement external seal?"
      result.heading mustBe "What is the identification number for the replacement external seal?"
      result.additionalHtml mustBe Some(
        AdditionalHtml(
          paragraph1 = "Only enter a replacement seal affixed by a customs authority.",
          paragraph2 = "If this seal is broken, you must",
          linkText = "select no to using the revised unloading procedure",
          linkHref = controllers.routes.NewAuthYesNoController.onPageLoad(arrivalId, mode),
          paragraph3 = "You will then need to unload the goods and report any discrepancies."
        )
      )
      result.requiredError mustBe "Enter the identification number for the replacement external seal"
      result.maxLengthError mustBe "The identification number must be 512 characters or less"
      result.invalidError mustBe "The identification number must only include letters a to z without accents, numbers 0 to 9, ampersands (&), apostrophes, at signs (@), commas, forward slashes, full stops, hyphens, question marks and spaces"
      result.onSubmitCall.url mustBe controllers.routes.OtherThingsToReportController.onSubmit(arrivalId, mode).url
    }
  }
}
