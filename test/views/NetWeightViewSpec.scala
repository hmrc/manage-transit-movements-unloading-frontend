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

import forms.NetWeightFormProvider
import models.NormalMode
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.NetWeightView

class NetWeightViewSpec extends InputTextViewBehaviours[String] {

  override def form: Form[String] = new NetWeightFormProvider()()

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[NetWeightView].apply(form, mrn, arrivalId, index, index, NormalMode)(fakeRequest, messages)

  override val prefix: String = "netWeight"

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(index.display.toString)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(index.display.toString)

  behave like pageWithContent("p", "This is the weight of the item’s goods, excluding all packaging.")

  behave like pageWithHint("Enter the weight in kilograms (kg), up to 6 decimal places.")

  behave like pageWithInputText(inputFieldClassSize = Some(InputSize.Width10), suffix = Some("kg"))

  behave like pageWithSubmitButton("Continue")
}
