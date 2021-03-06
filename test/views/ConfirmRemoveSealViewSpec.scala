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

import forms.ConfirmRemoveSealFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.ConfirmRemoveSealView

class ConfirmRemoveSealViewSpec extends YesNoViewBehaviours {

  private val sealDescription      = "sealDescription"
  override def form: Form[Boolean] = new ConfirmRemoveSealFormProvider()(sealDescription)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[ConfirmRemoveSealView].apply(form, mrn, arrivalId, Index(1), sealDescription, NormalMode)(fakeRequest, messages)

  override val prefix: String = "confirmRemoveSeal"

  behave like pageWithBackLink

  behave like pageWithCaption(mrn.toString)

  behave like pageWithRadioItems(args = List(sealDescription))

  behave like pageWithSubmitButton("Continue")
}
