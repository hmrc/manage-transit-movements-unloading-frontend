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

package views.houseConsignment.index.additionalReference

import base.SpecBase
import forms.Constants.maxAdditionalReferenceNumLength
import forms.HouseConsignmentAdditionalReferenceNumberFormProvider
import generators.Generators
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.CharacterCountViewBehaviours
import views.html.houseConsignment.index.additionalReference.AdditionalReferenceNumberView

class AdditionalReferenceNumberViewSpec extends SpecBase with CharacterCountViewBehaviours with Generators {

  private val formProvider = new HouseConsignmentAdditionalReferenceNumberFormProvider()

  override def form: Form[String] = formProvider(prefix)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[AdditionalReferenceNumberView]
      .apply(form, arrivalId, mrn, NormalMode, houseConsignmentIndex, additionalReferenceIndex)(fakeRequest, messages)

  override val prefix: String =
    "houseConsignment.index.additionalReference.additionalReferenceNumber"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithCharacterCount(maxAdditionalReferenceNumLength)

  behave like pageWithSubmitButton("Continue")

}
