/*
 * Copyright 2025 HM Revenue & Customs
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

import forms.UniqueConsignmentReferenceFormProvider
import generators.Generators
import models.NormalMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.{InputSize, UniqueConsignmentReferenceViewModel}
import views.behaviours.InputTextViewBehaviours
import views.html.UniqueConsignmentReferenceView

class UniqueConsignmentReferenceViewSpec extends InputTextViewBehaviours[String] with Generators {

  override val prefix: String = "uniqueConsignmentReference"

  override def form: Form[String] = new UniqueConsignmentReferenceFormProvider()(prefix, viewModel.requiredError)

  private val viewModel: UniqueConsignmentReferenceViewModel = arbitrary[UniqueConsignmentReferenceViewModel].sample.value

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[UniqueConsignmentReferenceView].apply(form, mrn, arrivalId, NormalMode, viewModel)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithHint("This can be up to 70 characters long and include both letters and numbers.")

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Continue")
}
