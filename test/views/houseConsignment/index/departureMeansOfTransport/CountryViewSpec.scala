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

import forms.SelectableFormProvider
import generators.Generators
import models.reference.Country
import models.{CheckMode, NormalMode, SelectableList}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.departureTransportMeans.HouseConsignmentCountryViewModel
import views.behaviours.InputSelectViewBehaviours
import views.html.houseConsignment.index.departureMeansOfTransport.CountryView

class CountryViewSpec extends InputSelectViewBehaviours[Country] with Generators {
  private val mode                                        = Gen.oneOf(NormalMode, CheckMode).sample.value
  override def form: Form[Country]                        = new SelectableFormProvider()(mode, prefix, SelectableList(values), houseConsignmentIndex, dtmIndex)
  private val viewModel: HouseConsignmentCountryViewModel = arbitrary[HouseConsignmentCountryViewModel].sample.value

  override def applyView(form: Form[Country]): HtmlFormat.Appendable =
    injector
      .instanceOf[CountryView]
      .apply(form, values, mrn, arrivalId, houseConsignmentIndex, dtmIndex, NormalMode, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = "houseConsignment.index.departureMeansOfTransport.country"

  implicit override val arbitraryT: Arbitrary[Country] = arbitraryCountry

  val countrySeq: Seq[Country] = Seq(
    Country("UK", "United Kingdom"),
    Country("US", "United States"),
    Country("ES", "Spain")
  )

  val selectableListCountries: SelectableList[Country] = SelectableList(countrySeq)

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithSelect()

  behave like pageWithHint("Enter the country or code, like Austria or AT.")

  behave like pageWithSubmitButton("Continue")

}
