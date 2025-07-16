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

package views.houseConsignment.index.items

import base.SpecBase
import forms.CommodityCodeFormProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.items.CommodityCodeViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.index.items.CommodityCodeView

class CommodityCodeViewSpec extends InputTextViewBehaviours[String] with SpecBase {

  private val viewModel = arbitrary[CommodityCodeViewModel].sample.value

  override def form: Form[String] = new CommodityCodeFormProvider()(viewModel.requiredError)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[CommodityCodeView].apply(form, mrn, isXI = true, viewModel)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "houseConsignment.commodityCode.NormalMode",
      "houseConsignment.commodityCode.CheckMode"
    )
    .sample
    .value

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  private val paragraph =
    "The combination of your commodity code and combined nomenclature code must be a valid code in TARIC. This is the European Union’s database for classifying goods and determining the amount of duties required."

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithHint("This is 6 numbers long.")

  behave like pageWithInputText()

  behave like pageWithSubmitButton("Continue")

  "when isXI is true" - {

    behave like pageWithContent("p", paragraph)
  }

  "when isXI is false" - {
    val view = injector.instanceOf[CommodityCodeView]
    val doc  = parseView(view.apply(form, mrn, isXI = false, viewModel)(fakeRequest, messages))
    behave like pageWithoutContent(doc, "p", paragraph)
  }

}
