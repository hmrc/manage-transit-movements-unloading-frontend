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
import forms.DepartureMeansOfTransportCountryFormProvider
import models.{CheckMode, NormalMode}
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.twirl.api.Html
import viewModels.components.InputSelectViewModel.{AddressCountrySelect, OrdinarySelect, SelectWithAdditionalHtml}
import views.html.components.InputSelect
import views.html.templates.MainTemplate

class InputSelectSpec extends A11ySpecBase {

  "the 'input select' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[InputSelect]
    val mode      = Gen.oneOf(NormalMode, CheckMode).sample.value

    val title          = nonEmptyString.sample.value
    val caption        = Gen.option(nonEmptyString).sample.value
    val countries      = distinctListWithMaxLength[Country, String]()(_.code).sample.value
    val label          = nonEmptyString.sample.value
    val hint           = Gen.option(nonEmptyString).sample.value
    val placeholder    = nonEmptyString.sample.value
    val selectedValue  = Gen.oneOf(None, Some(countries.head)).sample.value
    val selectItems    = countries.toSelectItems(selectedValue)
    val additionalHtml = arbitrary[Html].sample.value
    val form           = new DepartureMeansOfTransportCountryFormProvider()(mode, countries)
    val preparedForm = selectedValue match {
      case Some(customsOffice) => form.fill(customsOffice)
      case None                => form
    }

    "pass accessibility checks" when {

      "ordinary select" in {
        val content = template.apply(title) {
          component.apply(preparedForm("value"), OrdinarySelect(title, caption), placeholder, selectItems, hint)
        }
        content.toString() must passAccessibilityChecks
      }

      "select with additional html" in {
        val content = template.apply(title) {
          component.apply(preparedForm("value"), SelectWithAdditionalHtml(title, caption, additionalHtml), placeholder, selectItems, hint)
        }
        content.toString() must passAccessibilityChecks
      }

      "address country select" in {
        val content = template.apply(title) {
          component.apply(preparedForm("value"), AddressCountrySelect(label), placeholder, selectItems, hint).withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }
    }
  }
}
