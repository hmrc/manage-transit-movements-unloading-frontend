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

package views

import forms.AnythingElseToReportFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.AnythingElseToReportView

class AnythingElseToReportViewSpec extends YesNoViewBehaviours {

  override def form: Form[Boolean] = new AnythingElseToReportFormProvider()()

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[AnythingElseToReportView].apply(form, mrn, arrivalId, NormalMode)(fakeRequest, messages)

  override val prefix: String = "anythingElseToReport"

  behave like pageWithBackLink

  behave like pageWithHeading()

  behave like pageWithRadioItems(legendIsHeading = false, None, legendIsVisible = false)

  behave like pageWithSubmitButton("Continue")
}
