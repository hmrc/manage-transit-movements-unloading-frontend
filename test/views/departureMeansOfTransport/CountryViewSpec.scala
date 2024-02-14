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

package views.departureMeansOfTransport

import forms.DepartureMeansOfTransportCountryFormProvider
import generators.Generators
import models.{CheckMode, NormalMode}
import models.reference.Country
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.departureTransportMeans.CountryViewModel
import views.behaviours.InputSelectViewBehaviours
import views.html.departureMeansOfTransport.CountryView

class CountryViewSpec extends InputSelectViewBehaviours[Country] with Generators {
  private val mode                        = Gen.oneOf(NormalMode, CheckMode).sample.value
  override def form: Form[Country]        = new DepartureMeansOfTransportCountryFormProvider()(mode, values)
  private val viewModel: CountryViewModel = arbitrary[CountryViewModel].sample.value

  override def applyView(form: Form[Country]): HtmlFormat.Appendable =
    injector.instanceOf[CountryView].apply(form, values, mrn, arrivalId, index, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = "departureMeansOfTransport.country"

  implicit override val arbitraryT: Arbitrary[Country] = arbitraryCountry

  override lazy val values: Seq[Country] = Seq(
    Country("UK", "United Kingdom"),
    Country("US", "United States"),
    Country("ES", "Spain")
  )

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithSelect()

  behave like pageWithHint("Enter the country or code, like Austria or AT.")

  behave like pageWithSubmitButton("Continue")

}
