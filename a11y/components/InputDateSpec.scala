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

package components

import a11ySpecBase.A11ySpecBase
import forms.DateGoodsUnloadedFormProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.twirl.api.Html
import services.DateTimeService
import viewModels.components.InputDateViewModel.{DateInputWithAdditionalHtml, OrdinaryDateInput}
import views.html.components.InputDate
import views.html.templates.MainTemplate

import java.time.LocalDate

class InputDateSpec extends A11ySpecBase {

  "the 'input date' component" must {
    val template        = app.injector.instanceOf[MainTemplate]
    val component       = app.injector.instanceOf[InputDate]
    val dateTimeService = app.injector.instanceOf[DateTimeService]

    val minDate = arbitrary[LocalDate].sample.value
    val title   = nonEmptyString.sample.value
    val hint    = Gen.option(nonEmptyString).sample.value
    val caption = Gen.option(nonEmptyString).sample.value
    val form    = new DateGoodsUnloadedFormProvider(dateTimeService)(minDate)

    val html = Html("<p>test</p>")

    "pass accessibility checks for OrdinaryDateInput" in {

      val viewModel = OrdinaryDateInput(title, caption)

      val content = template.apply(title) {
        component.apply(form = form, dateType = viewModel, hint = hint)
      }
      content.toString() must passAccessibilityChecks
    }

    "pass accessibility checks for DateInputWithAdditionalHtml" in {

      val viewModel = DateInputWithAdditionalHtml(title, caption, html)

      val content = template.apply(title) {
        component.apply(form = form, dateType = viewModel, hint = hint)
      }
      content.toString() must passAccessibilityChecks
    }
  }
}
