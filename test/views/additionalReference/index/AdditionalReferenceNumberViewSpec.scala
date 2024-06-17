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

package views.additionalReference.index

import forms.AdditionalReferenceNumberFormProvider
import models.{CheckMode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.additionalReference.index.AdditionalReferenceNumberViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.additionalReference.index.AdditionalReferenceNumberView

class AdditionalReferenceNumberViewSpec extends InputTextViewBehaviours[String] {

  private val viewModel = arbitrary[AdditionalReferenceNumberViewModel].sample.value
  private val mode      = Gen.oneOf(NormalMode, CheckMode).sample.value

  override def form: Form[String] = new AdditionalReferenceNumberFormProvider()(viewModel.requiredError)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[AdditionalReferenceNumberView]
      .apply(form, arrivalId, mrn, additionalReferenceIndex, mode, viewModel)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "additionalReference.index.NormalMode",
      "additionalReference.index.CheckMode"
    )
    .sample
    .value

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithHint("This can be up to 70 characters long.")

  behave like pageWithSubmitButton("Continue")

}
