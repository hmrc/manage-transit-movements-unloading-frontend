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

package views.houseConsignment.index.documents

import forms.ReferenceNumberFormProvider
import models.{CheckMode, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.documents.ReferenceNumberViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.index.documents.ReferenceNumberView

class ReferenceNumberViewSpec extends InputTextViewBehaviours[String] {

  private val viewModel: ReferenceNumberViewModel =
    arbitrary[ReferenceNumberViewModel].sample.value

  private val mode: Mode          = Gen.oneOf(NormalMode, CheckMode).sample.value
  override def form: Form[String] = new ReferenceNumberFormProvider()(viewModel.requiredError, houseConsignmentIndex, Seq.empty)

  def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[ReferenceNumberView]
      .apply(form, mrn, arrivalId, mode, viewModel, houseConsignmentIndex, documentIndex)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "houseConsignment.index.documents.referenceNumber.normalMode",
      "houseConsignment.index.documents.referenceNumber.checkMode"
    )
    .sample
    .value

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithHint("This can be up to 70 characters long and include both letters and numbers.")

  behave like pageWithInputText()

  behave like pageWithSubmitButton("Continue")

}
