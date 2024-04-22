/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.EnumerableFormProvider
import generators.Generators
import models.reference.TransportMeansIdentification
import models.{CheckMode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewModels.houseConsignment.index.departureTransportMeans.IdentificationViewModel
import views.behaviours.EnumerableViewBehaviours
import views.html.houseConsignment.index.departureMeansOfTransport.IdentificationView

class IdentificationViewSpec extends EnumerableViewBehaviours[TransportMeansIdentification] with Generators {
  private val mode                                      = Gen.oneOf(NormalMode, CheckMode).sample.value
  override def form: Form[TransportMeansIdentification] = new EnumerableFormProvider()(mode, prefix, values)
  private val viewModel: IdentificationViewModel        = arbitrary[IdentificationViewModel].sample.value

  override def applyView(form: Form[TransportMeansIdentification]): HtmlFormat.Appendable =
    injector.instanceOf[IdentificationView].apply(form, mrn, arrivalId, hcIndex, index, values, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = "houseConsignment.index.departureMeansOfTransport.identification"

  override def radioItems(fieldId: String, checkedValue: Option[TransportMeansIdentification] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[TransportMeansIdentification] = Seq(
    TransportMeansIdentification("80", "European vessel identification number (ENI Code)"),
    TransportMeansIdentification("81", "Name of an inland waterways vehicle")
  )

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithRadioItems(args = Seq(index.display), mode = Some(mode))

  behave like pageWithSubmitButton("Continue")

  if (viewModel.paragraph.isDefined) {
    behave like pageWithContent("p", viewModel.paragraph.get)
  }
}
