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

package views.houseConsignment.index.items.packages

import forms.Constants.maxPackageShippingMarkLength
import forms.PackageShippingMarkFormProvider
import generators.Generators
import models.NormalMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.items.packages.PackageShippingMarksViewModel
import views.behaviours.CharacterCountViewBehaviours
import views.html.houseConsignment.index.items.packages.PackageShippingMarkView

class PackageShippingMarkViewSpec extends CharacterCountViewBehaviours with Generators {

  private val viewModel: PackageShippingMarksViewModel =
    arbitrary[PackageShippingMarksViewModel].sample.value

  def form: Form[String] = new PackageShippingMarkFormProvider()(viewModel.requiredError)

  def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[PackageShippingMarkView]
      .apply(form, mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "houseConsignment.index.item.packageShippingMark.normalMode",
      "houseConsignment.index.item.packageShippingMark.checkMode"
    )
    .sample
    .value

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithHint("You can enter up to 512 characters")

  behave like pageWithCharacterCount(maxPackageShippingMarkLength)

  behave like pageWithSubmitButton("Continue")

}
