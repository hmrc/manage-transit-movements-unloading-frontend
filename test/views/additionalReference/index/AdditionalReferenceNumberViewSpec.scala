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

import base.SpecBase
import forms.Constants.maxAdditionalReferenceNumLength
import forms.AdditionalReferenceNumberFormProvider
import generators.Generators
import models.{CheckMode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.additionalReference.index.AdditionalReferenceNumberViewModel
import views.behaviours.CharacterCountViewBehaviours
import views.html.additionalReference.index.AdditionalReferenceNumberView

class AdditionalReferenceNumberViewSpec extends SpecBase with CharacterCountViewBehaviours with Generators {

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

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithCharacterCount(maxAdditionalReferenceNumLength)

  behave like pageWithSubmitButton("Continue")

  private val content = "You need to enter a reference number as you have already added this type of additional reference."

  "when paragraph required" - {
    "must render paragraph" - {
      val view = injector
        .instanceOf[AdditionalReferenceNumberView]
        .apply(form, arrivalId, mrn, additionalReferenceIndex, mode, viewModel.copy(isParagraphRequired = true))(fakeRequest, messages)

      val doc = parseView(view)

      behave like pageWithContent(
        doc = doc,
        tag = "p",
        expectedText = content
      )
    }
  }
}
