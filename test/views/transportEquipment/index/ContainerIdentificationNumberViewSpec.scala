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

package views.transportEquipment.index

import forms.ContainerIdentificationNumberFormProvider
import models.NormalMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.transportEquipment.index.ContainerIdentificationNumberViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.transportEquipment.index.ContainerIdentificationNumberView

class ContainerIdentificationNumberViewSpec extends InputTextViewBehaviours[String] {

  private val viewModel: ContainerIdentificationNumberViewModel =
    arbitrary[ContainerIdentificationNumberViewModel].sample.value

  override def form: Form[String] = new ContainerIdentificationNumberFormProvider()(prefix, Seq.empty)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[ContainerIdentificationNumberView].apply(form, arrivalId, mrn, equipmentIndex, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "containerIdentificationNumber.NormalMode",
      "containerIdentificationNumber.CheckMode"
    )
    .sample
    .value

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithContent("p", viewModel.paragraph)

  behave like pageWithHint("This can be up to 17 characters long and include both letters and numbers, for example AABB3322110.")

  behave like pageWithInputText()

  behave like pageWithSubmitButton("Continue")
}
