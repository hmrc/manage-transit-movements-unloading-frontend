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

import forms.CUSCodeFormProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import viewModels.houseConsignment.index.items.CustomsUnionAndStatisticsCodeViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.index.items.CustomsUnionAndStatisticsCodeView

class CustomsUnionAndStatisticsCodeViewSpec extends InputTextViewBehaviours[String] {

  private val viewModel = arbitrary[CustomsUnionAndStatisticsCodeViewModel].sample.value

  override val prefix: String = Gen
    .oneOf(
      "houseConsignment.item.customsUnionAndStatisticsCode.NormalMode",
      "houseConsignment.item.customsUnionAndStatisticsCode.CheckMode"
    )
    .sample
    .value

  override def form: Form[String] = new CUSCodeFormProvider()(prefix, viewModel.requiredError)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[CustomsUnionAndStatisticsCodeView]
      .apply(form, mrn, viewModel)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithHint("This will be 9 characters long and can include letters, numbers and a hyphen. For example, 1234567-1.")

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Continue")
}
