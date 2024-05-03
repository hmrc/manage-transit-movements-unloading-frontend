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

package views.houseConsignment.index.items.packages

import forms.NumberOfPackagesFormProvider
import models.CheckMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import viewModels.houseConsignment.index.items.packages.NumberOfPackagesViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.index.items.packages.NumberOfPackagesView

class NumberOfPackagesViewSpec extends InputTextViewBehaviours[BigInt] {

  private val mode                                 = CheckMode
  private val viewModel: NumberOfPackagesViewModel = arbitrary[NumberOfPackagesViewModel].sample.value

  override val prefix: String = Gen
    .oneOf(
      "numberOfPackages.normalMode",
      "numberOfPackages.checkMode"
    )
    .sample
    .value

  override def form: Form[BigInt] = new NumberOfPackagesFormProvider()(prefix, BigInt(0), Seq.empty)

  override def applyView(form: Form[BigInt]): HtmlFormat.Appendable =
    injector
      .instanceOf[NumberOfPackagesView]
      .apply(form, arrivalId, mrn, hcIndex, itemIndex, index, mode, mode, mode, viewModel)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[BigInt] = Arbitrary(positiveBigInts)

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithoutHint()

  behave like pageWithInputText(Some(InputSize.Width10), Some("numeric"), Some("[0-9]*"))

  behave like pageWithSubmitButton("Continue")

}
