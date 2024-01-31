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
import forms.UnloadingCommentsFormProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.twirl.api.Html
import viewModels.components.InputCharacterCountViewModel.{InputCharacterCountWithAdditionalHtml, OrdinaryInputCharacterCount}
import views.html.components.InputCharacterCount
import views.html.templates.MainTemplate

class InputCharacterCountSpec extends A11ySpecBase {

  "the 'input character count' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[InputCharacterCount]

    val title          = nonEmptyString.sample.value
    val label          = nonEmptyString.sample.value
    val maxLength      = positiveInts.sample.value
    val caption        = Gen.option(nonEmptyString).sample.value
    val hint           = Gen.option(nonEmptyString).sample.value
    val rows           = positiveInts.sample.value
    val form           = new UnloadingCommentsFormProvider()()
    val additionalHtml = arbitrary[Html].sample.value

    "pass accessibility checks" when {

      "ordinary input character count" in {
        val content = template.apply(title) {
          component.apply(form("value"), label, maxLength, caption, hint, rows, OrdinaryInputCharacterCount(title, caption))
        }
        content.toString() must passAccessibilityChecks
      }

      "input character count with additional html" in {
        val content = template.apply(title) {
          component.apply(form("value"), label, maxLength, caption, hint, rows, InputCharacterCountWithAdditionalHtml(title, caption, additionalHtml))
        }
        content.toString() must passAccessibilityChecks
      }
    }
  }
}
