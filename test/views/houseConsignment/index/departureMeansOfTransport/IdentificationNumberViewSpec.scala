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

package views.houseConsignment.index.departureMeansOfTransport

import forms.VehicleIdentificationNumberFormProvider
import models.{CheckMode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.departureTransportMeans.IdentificationNumberViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.index.departureMeansOfTransport.IdentificationNumberView

class IdentificationNumberViewSpec extends InputTextViewBehaviours[String] {
  private val mode = Gen.oneOf(NormalMode, CheckMode).sample.value

  override def form: Form[String] = new VehicleIdentificationNumberFormProvider()(prefix, mode)
  private val viewModel           = arbitrary[IdentificationNumberViewModel].sample.value

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[IdentificationNumberView]
      .apply(form, mrn, arrivalId, houseConsignmentIndex, index, NormalMode, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = "houseConsignment.index.departureMeansOfTransport.identificationNumber"

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithHint("This can be up to 35 characters long and include both letters and numbers.")

  behave like pageWithInputText()

  behave like pageWithSubmitButton("Continue")
}
