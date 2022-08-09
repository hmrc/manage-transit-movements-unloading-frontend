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

package components

import a11ySpecBase.A11ySpecBase
import forms.DateGoodsUnloadedFormProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import views.html.components.InputDate
import views.html.templates.MainTemplate

import java.time.{Clock, LocalDate}

class InputDateSpec extends A11ySpecBase {

  "the 'input date' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[InputDate]
    val clock     = app.injector.instanceOf[Clock]

    val minDate     = arbitrary[LocalDate].sample.value
    val title       = nonEmptyString.sample.value
    val legend      = nonEmptyString.sample.value
    val legendClass = Gen.alphaNumStr.sample.value
    val hint        = Gen.option(nonEmptyString).sample.value
    val form        = new DateGoodsUnloadedFormProvider(clock)(minDate)

    "pass accessibility checks" when {

      "legend is heading" in {
        val content = template.apply(title) {
          val legendIsHeading = true
          component.apply(form("value"), legend, legendClass, hint, legendIsHeading)
        }
        content.toString() must passAccessibilityChecks
      }

      "legend isn't heading" in {
        val content = template.apply(title) {
          val legendIsHeading = false
          component.apply(form("value"), legend, legendClass, hint, legendIsHeading).withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }
    }
  }
}
