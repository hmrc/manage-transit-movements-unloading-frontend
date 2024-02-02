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

package views

import forms.NumberOfPackagesFormProvider
import viewModels.houseConsignment.index.items.NumberOfPackagesViewModel
import models.NormalMode
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.index.items.NumberOfPackagesView

class NumberOfPackagesViewSpec extends InputTextViewBehaviours[String] {

  private val viewModel = NumberOfPackagesViewModel(messages("numberOfPackages.normalMode.heading"),
                                                    messages("numberOfPackages.normalMode.title"),
                                                    messages("numberOfPackages.normalMode.error.required")
  )
  override def form: Form[String] = new NumberOfPackagesFormProvider()(viewModel.requiredError)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[NumberOfPackagesView]
      .apply(form, arrivalId, mrn, hcIndex, itemIndex, index, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = "numberOfPackages.normalMode"

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.oneOf(1 to 100).toString)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithoutHint()

  behave like pageWithInputText(Some(InputSize.Width10), Some("numeric"), Some("[0-9]*"))

  behave like pageWithSubmitButton("Continue")
}
