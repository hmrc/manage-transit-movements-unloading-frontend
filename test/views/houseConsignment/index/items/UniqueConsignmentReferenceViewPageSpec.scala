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

package views.houseConsignment.index.items

import forms.UniqueConsignmentReferenceFormProvider
import models.{Mode, NormalMode}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import viewModels.houseConsignment.index.UniqueConsignmentReferenceViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.index.UniqueConsignmentReferenceView

class UniqueConsignmentReferenceViewPageSpec extends InputTextViewBehaviours[String] {

  override val prefix: String = "houseConsignment.uniqueConsignmentReference"

  private val mode: Mode = NormalMode

  override def form: Form[String] = new UniqueConsignmentReferenceFormProvider()(prefix, mode)

  private val viewModel: UniqueConsignmentReferenceViewModel = arbitrary[UniqueConsignmentReferenceViewModel].sample.value

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[UniqueConsignmentReferenceView].apply(form, mrn, arrivalId, index, NormalMode, viewModel)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithHint("This can be up to 70 characters long and include both letters and numbers.")

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Continue")
}
