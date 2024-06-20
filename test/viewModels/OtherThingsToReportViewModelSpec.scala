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
      val result            = viewModelProvider.apply(arrivalId, NormalMode, newAuth = false)

      result.prefix mustBe "otherThingsToReport.oldAuth"
      result.title mustBe "What do you want to report?"
      result.heading mustBe "What do you want to report?"
      result.hint must not be defined
      result.requiredError mustBe "Enter what you want to report"
      result.newAuthLink().url mustBe controllers.routes.NewAuthYesNoController.onPageLoad(arrivalId, NormalMode).url
      result.onSubmit().url mustBe controllers.routes.OtherThingsToReportController.onSubmit(arrivalId, NormalMode).url
    }

    "when newAuth is true" in {
      val viewModelProvider = new OtherThingsToReportViewModelProvider()
      val result            = viewModelProvider.apply(arrivalId, NormalMode, newAuth = true)

      result.prefix mustBe "otherThingsToReport.newAuth"
      result.title mustBe "Enter all the seal identification numbers"
      result.heading mustBe "Enter all the seal identification numbers"
      result.hint mustBe Some("Each seal can be up to 20 characters long and include both letters and numbers.")
      result.requiredError mustBe "Enter all the seal identification numbers"
      result.newAuthLink().url mustBe controllers.routes.NewAuthYesNoController.onPageLoad(arrivalId, NormalMode).url
      result.onSubmit().url mustBe controllers.routes.OtherThingsToReportController.onSubmit(arrivalId, NormalMode).url
    }
  }
}
