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

package controllers.houseConsignment.index.departureMeansOfTransport

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.SelectableFormProvider.CountryFormProvider
import generators.Generators
import models.reference.Country
import models.{NormalMode, SelectableList}
import navigation.houseConsignment.index.departureMeansOfTransport.DepartureTransportMeansNavigator.DepartureTransportMeansNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.departureMeansOfTransport.CountryPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ReferenceDataService
import viewModels.houseConsignment.index.departureTransportMeans.HouseConsignmentCountryViewModel
import viewModels.houseConsignment.index.departureTransportMeans.HouseConsignmentCountryViewModel.HouseConsignmentCountryViewModelProvider
import views.html.houseConsignment.index.departureMeansOfTransport.CountryView

import scala.concurrent.Future

class CountryControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val houseConsignmentMode = NormalMode
  private val transportMeansMode   = NormalMode

  private val country: Country                            = Country("GB", "United Kingdom")
  val countries: Seq[Country]                             = Seq(Country("GB", "United Kingdom"))
  val countryList: SelectableList[Country]                = SelectableList(countries)
  private val mockViewModelProvider                       = mock[HouseConsignmentCountryViewModelProvider]
  private val viewModel: HouseConsignmentCountryViewModel = arbitrary[HouseConsignmentCountryViewModel].sample.value
  private val prefix                                      = "houseConsignment.index.departureMeansOfTransport.country"

  private val formProvider = new CountryFormProvider()
  private val field        = formProvider.field
  def form: Form[Country]  = formProvider(transportMeansMode, prefix, SelectableList(countries), 1)

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  lazy val DepartureMeansOfTransportCountryRoute: String =
    routes.CountryController.onPageLoad(arrivalId, houseConsignmentIndex, dtmIndex, houseConsignmentMode, transportMeansMode).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockViewModelProvider)
    when(mockViewModelProvider.apply(any(), any())(any())).thenReturn(viewModel)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[DepartureTransportMeansNavigatorProvider]).toInstance(FakeHouseConsignmentNavigators.fakeDepartureTransportMeansNavigatorProvider),
        bind[ReferenceDataService].toInstance(mockReferenceDataService),
        bind[HouseConsignmentCountryViewModelProvider].toInstance(mockViewModelProvider)
      )

  "departureMeansOfTransportCountry Controller" - {

    "must return OK and the correct view for a GET" in {
      when(mockReferenceDataService.getCountries()(any())).thenReturn(Future.successful(countries))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, DepartureMeansOfTransportCountryRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[CountryView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(form, countryList.values, mrn, arrivalId, houseConsignmentIndex, dtmIndex, houseConsignmentMode, transportMeansMode, viewModel)(request,
                                                                                                                                             messages
        ).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      when(mockReferenceDataService.getCountries()(any())).thenReturn(
        Future.successful(countries)
      )

      val userAnswers = emptyUserAnswers.setValue(CountryPage(houseConsignmentIndex, dtmIndex), country)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, DepartureMeansOfTransportCountryRoute)
      val result  = route(app, request).value

      val filledForm = form.bind(Map(field -> "GB"))

      val view = injector.instanceOf[CountryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, countries, mrn, arrivalId, houseConsignmentIndex, dtmIndex, houseConsignmentMode, transportMeansMode, viewModel)(request,
                                                                                                                                          messages
        ).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)
      when(mockReferenceDataService.getCountries()(any())).thenReturn(Future.successful(countries))

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, DepartureMeansOfTransportCountryRoute)
          .withFormUrlEncodedBody((field, "GB"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      when(mockReferenceDataService.getCountries()(any())).thenReturn(Future.successful(countries))

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, DepartureMeansOfTransportCountryRoute).withFormUrlEncodedBody((field, ""))
      val boundForm = form.bind(Map(field -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[CountryView]

      contentAsString(result) mustEqual
        view(boundForm, countries, mrn, arrivalId, houseConsignmentIndex, dtmIndex, houseConsignmentMode, transportMeansMode, viewModel)(request,
                                                                                                                                         messages
        ).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, DepartureMeansOfTransportCountryRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, DepartureMeansOfTransportCountryRoute)
          .withFormUrlEncodedBody((field, "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

  }
}
