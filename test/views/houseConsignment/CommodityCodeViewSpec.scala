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

package views.houseConsignment

import forms.CommodityCodeFormProvider
import models.NormalMode
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.CommodityCodeView

class CommodityCodeViewSpec extends InputTextViewBehaviours[String] {

  override def form: Form[String] = new CommodityCodeFormProvider()(index, index)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[CommodityCodeView].apply(form, mrn, arrivalId, index, index, isXI, NormalMode)(fakeRequest, messages)

  override val prefix: String                         = "commodityCode"
  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(index.display.toString, index.display.toString)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(index.display.toString, index.display.toString)

  behave like pageWithHint("This will be 6 characters long and include both letters and numbers, for example 0G23AB.")

  behave like pageWithInputText()

  behave like pageWithSubmitButton("Continue")

  if (isXI) {

    behave like pageWithContent(
      "p",
      "The combination of your commodity code and combined nomenclature code must be a valid code in TARIC. This is the European Union’s database for classifying goods and determining the amount of duties required."
    )
  }

}
