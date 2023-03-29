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
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.Html
import viewModels.sections.Section
import views.html.components.SummaryDetail
import views.html.templates.MainTemplate

class SummaryDetailSpec extends A11ySpecBase {

  "the 'answer section' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[SummaryDetail]

    val title   = nonEmptyString.sample.value
    val section = arbitrary[Section].sample.value
    val html    = arbitrary[Html].sample.value

    val content = template.apply(title) {
      component.apply(section)(html).withHeading(title)
    }

    "pass accessibility checks" in {
      content.toString() must passAccessibilityChecks
    }
  }
}