/*
 * Copyright 2024 HM Revenue & Customs
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

package views.houseConsignment.index.items

import forms.Constants.maxItemDescriptionLength
import forms.DescriptionFormProvider
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.items.DescriptionViewModel
import views.behaviours.CharacterCountViewBehaviours
import views.html.houseConsignment.index.items.DescriptionView

class DescriptionViewSpec extends CharacterCountViewBehaviours with Generators {

  private val viewModel = arbitrary[DescriptionViewModel].sample.value

  private val formProvider = new DescriptionFormProvider()

  override def form: Form[String] = formProvider(viewModel.requiredError)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[DescriptionView].apply(form, mrn, viewModel)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "houseConsignment.item.description.NormalMode",
      "houseConsignment.item.description.CheckMode"
    )
    .sample
    .value

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithContent("p", "This should be clear and detailed enough for anyone involved in the transit movement to understand its contents.")

  behave like pageWithCharacterCount(maxItemDescriptionLength)

  behave like pageWithSubmitButton("Continue")
}
