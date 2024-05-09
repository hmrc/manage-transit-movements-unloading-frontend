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

package views.houseConsignment.index.items

import forms.WeightFormProvider
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import viewModels.houseConsignment.index.items.GrossWeightViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.index.items.GrossWeightView

class GrossWeightViewSpec extends InputTextViewBehaviours[BigDecimal] with Generators {

  private val viewModel = arbitrary[GrossWeightViewModel].sample.value

  private val decimalPlace: Int   = positiveInts.sample.value
  private val characterCount: Int = positiveInts.sample.value

  override def form: Form[BigDecimal] =
    app.injector.instanceOf[WeightFormProvider].apply(prefix, viewModel.requiredError, decimalPlace, characterCount)

  override def applyView(form: Form[BigDecimal]): HtmlFormat.Appendable =
    injector.instanceOf[GrossWeightView].apply(form, mrn, viewModel)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "houseConsignment.item.grossWeight.NormalMode",
      "houseConsignment.item.grossWeight.CheckMode"
    )
    .sample
    .value

  implicit override val arbitraryT: Arbitrary[BigDecimal] = Arbitrary(positiveBigDecimals)

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithContent("p", "This is the combined weight of the item’s goods and packaging.")

  behave like pageWithHint("Enter the weight in kilograms (kg), up to 6 decimal places.")

  behave like pageWithInputText(inputFieldClassSize = Some(InputSize.Width10), suffix = Some("kg"))

  behave like pageWithSubmitButton("Continue")
}
