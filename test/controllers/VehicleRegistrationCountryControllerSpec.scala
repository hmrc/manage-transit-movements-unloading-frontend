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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.VehicleRegistrationCountryFormProvider
import generators.Generators
import models.NormalMode
import models.reference.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.VehicleRegistrationCountryPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ReferenceDataService
import views.html.VehicleRegistrationCountryView

import scala.concurrent.Future

class VehicleRegistrationCountryControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  val formProvider                                   = new VehicleRegistrationCountryFormProvider()
  private val country: String                        = Country("GB", Some("United Kingdom")).code
  val countries: Seq[Country]                        = Seq(Country("GB", Some("United Kingdom")))
  val form: Form[Country]                            = formProvider(countries)
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]
  private val mode                                   = NormalMode
  lazy val vehicleRegistrationCountryRoute: String   = controllers.routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, index, mode).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "VehicleRegistrationCountry Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()

      when(mockReferenceDataService.getCountries()(any(), any())).thenReturn(Future.successful(countries))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, vehicleRegistrationCountryRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[VehicleRegistrationCountryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, countries, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      when(mockReferenceDataService.getCountries()(any(), any())).thenReturn(
        Future.successful(countries)
      )

      val userAnswers = emptyUserAnswers.setValue(VehicleRegistrationCountryPage(index), country)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, vehicleRegistrationCountryRoute)
      val result  = route(app, request).value

      val filledForm = form.bind(Map("value" -> "GB"))

      val view = injector.instanceOf[VehicleRegistrationCountryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, countries, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockReferenceDataService.getCountries()(any(), any())).thenReturn(Future.successful(countries))

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, vehicleRegistrationCountryRoute)
          .withFormUrlEncodedBody(("value", "GB"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      when(mockReferenceDataService.getCountries()(any(), any())).thenReturn(Future.successful(countries))

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, vehicleRegistrationCountryRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[VehicleRegistrationCountryView]

      contentAsString(result) mustEqual
        view(boundForm, countries, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, vehicleRegistrationCountryRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, vehicleRegistrationCountryRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

  }
}
