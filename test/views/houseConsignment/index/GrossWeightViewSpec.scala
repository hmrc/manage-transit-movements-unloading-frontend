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

package views.houseConsignment.index

import forms.WeightFormProvider
import models.NormalMode
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.index.GrossWeightView

class GrossWeightViewSpec extends InputTextViewBehaviours[BigDecimal] {

  private val decimalPlace: Int   = positiveInts.sample.value
  private val characterCount: Int = positiveInts.sample.value
  private val mode                = NormalMode

  override def form: Form[BigDecimal] = app.injector.instanceOf[WeightFormProvider].apply(prefix, decimalPlace, characterCount, isZeroAllowed = false)

  override def applyView(form: Form[BigDecimal]): HtmlFormat.Appendable =
    injector.instanceOf[GrossWeightView].apply(form, mrn, arrivalId, houseConsignmentIndex, mode)(fakeRequest, messages)
  override val prefix: String = s"houseConsignment.index.grossWeight.$mode"

  implicit override val arbitraryT: Arbitrary[BigDecimal] = Arbitrary(positiveBigDecimals)

  behave like pageWithTitle(houseConsignmentIndex.display)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(houseConsignmentIndex.display)

  behave like pageWithContent("p", "This is the combined weight of the house consignment’s goods and packaging.")

  behave like pageWithHint("Enter the weight in kilograms (kg), up to 6 decimal places.")

  behave like pageWithInputText(inputFieldClassSize = Some(InputSize.Width10), suffix = Some("kg"))

  behave like pageWithSubmitButton("Continue")
}
