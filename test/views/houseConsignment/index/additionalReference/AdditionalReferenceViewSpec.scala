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

import forms.SelectableFormProvider
import models.reference.AdditionalReferenceType
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.additionalReference.AdditionalReferenceTypeViewModel
import views.behaviours.InputSelectViewBehaviours
import views.html.houseConsignment.index.additionalReference.AdditionalReferenceTypeView

class AdditionalReferenceViewSpec extends InputSelectViewBehaviours[AdditionalReferenceType] {

  private val viewModel: AdditionalReferenceTypeViewModel =
    arbitrary[AdditionalReferenceTypeViewModel].sample.value

  override def form: Form[AdditionalReferenceType] = new SelectableFormProvider().apply(NormalMode, prefix, SelectableList(values))

  override def applyView(form: Form[AdditionalReferenceType]): HtmlFormat.Appendable =
    injector
      .instanceOf[AdditionalReferenceTypeView]
      .apply(form, mrn, values, viewModel)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[AdditionalReferenceType] = arbitraryAdditionalReference

  override val prefix: String =
    "houseConsignment.index.additionalReference.additionalReference"

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithSelect()

  behave like pageWithSubmitButton("Continue")

  behave like pageWithHint("Enter the reference name or code, like Carrier (AEO certificate number) or Y028.")
}
