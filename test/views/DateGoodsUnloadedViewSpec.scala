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

package views

import forms.DateGoodsUnloadedFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import services.DateTimeService
import views.behaviours.DateInputViewBehaviour
import views.html.DateGoodsUnloadedView

import java.time.LocalDate

class DateGoodsUnloadedViewSpec extends DateInputViewBehaviour {

  private val dateTimeService = app.injector.instanceOf[DateTimeService]

  override def form = new DateGoodsUnloadedFormProvider(dateTimeService)(LocalDate.now())

  override def applyView(form: Form[LocalDate]): HtmlFormat.Appendable =
    app.injector.instanceOf[DateGoodsUnloadedView].apply(mrn, arrivalId, NormalMode, form)(fakeRequest, messages)

  override val prefix: String = "dateGoodsUnloaded"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithHint("For example, 15 08 2022.")

  behave like pageWithDateInput()

}
