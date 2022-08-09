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
import forms.AreAnySealsBrokenFormProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.components.InputRadio
import views.html.templates.MainTemplate

class InputRadioSpec extends A11ySpecBase {

  "the 'input radio' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[InputRadio]

    val title      = nonEmptyString.sample.value
    val legend     = nonEmptyString.sample.value
    val hint       = Gen.option(nonEmptyString).sample.value
    val radioItems = (_: String) => listWithMaxLength[RadioItem]().sample.value
    val inline     = arbitrary[Boolean].sample.value
    val form       = new AreAnySealsBrokenFormProvider()()

    val content = template.apply(title) {
      component.apply(form("value"), legend, hint, radioItems, inline)
    }

    "pass accessibility checks" in {
      content.toString() must passAccessibilityChecks
    }
  }
}
