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

package views.transportEquipment.index.seals

import forms.SealIdentificationNumberFormProvider
import models.NormalMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.transportEquipment.index.seals.SealIdentificationNumberViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.transportEquipment.index.seals.SealIdentificationNumberView

class SealIdentificationNumberViewSpec extends InputTextViewBehaviours[String] {

  private val viewModel: SealIdentificationNumberViewModel =
    arbitrary[SealIdentificationNumberViewModel].sample.value

  private val formProvider = app.injector.instanceOf[SealIdentificationNumberFormProvider]

  override def form: Form[String] = formProvider.apply(viewModel.requiredError, Seq.empty)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[SealIdentificationNumberView]
      .apply(form, mrn, arrivalId, NormalMode, NormalMode, viewModel, equipmentIndex, sealIndex)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "transportEquipment.index.seal.identificationNumber.NormalMode",
      "transportEquipment.index.seal.identificationNumber.CheckMode"
    )
    .sample
    .value

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHint("This can be up to 20 characters long and include both letters and numbers.")

  behave like pageWithInputText()

  behave like pageWithSubmitButton("Continue")
}
