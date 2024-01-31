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

import forms.EnumerableFormProvider
import models.{NormalMode, UnloadingType}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.behaviours.RadioViewBehaviours
import views.html.UnloadingTypeView

class UnloadingTypeViewSpec extends RadioViewBehaviours[UnloadingType] {

  override val getValue: UnloadingType => String = _.toString

  override def form: Form[UnloadingType] = new EnumerableFormProvider()(prefix)

  override def applyView(form: Form[UnloadingType]): HtmlFormat.Appendable =
    injector.instanceOf[UnloadingTypeView].apply(form, mrn, UnloadingType.values, arrivalId, NormalMode)(fakeRequest, messages)

  override val prefix: String = "unloadingType"

  override def radioItems(fieldId: String, checkedValue: Option[UnloadingType] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[UnloadingType] = UnloadingType.values

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Continue")
}
