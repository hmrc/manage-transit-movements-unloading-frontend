/*
 * Copyright 2022 HM Revenue & Customs
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
import matchers.JsonMatchers
import models.NormalMode
import models.reference.Country
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import pages.VehicleRegistrationCountryPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.ReferenceDataService
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class VehicleRegistrationCountryControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val formProvider                                   = new VehicleRegistrationCountryFormProvider()
  private val country: Country                       = Country("GB", "United Kingdom")
  val countries                                      = Seq(country)
  val form: Form[Country]                            = formProvider(countries)
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]
  lazy val vehicleRegistrationCountryRoute: String   = routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, NormalMode).url

  def countriesJson(selected: Boolean = false) = Seq(
    Json.obj("text" -> "", "value"               -> ""),
    Json.obj("text" -> "United Kingdom", "value" -> "GB", "selected" -> selected)
  )

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
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockReferenceDataService.getCountries()(any(), any())).thenReturn(Future.successful(countries))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, vehicleRegistrationCountryRoute)

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"      -> form,
        "mrn"       -> mrn,
        "mode"      -> NormalMode,
        "countries" -> countriesJson()
      )

      templateCaptor.getValue mustEqual "vehicleRegistrationCountry.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockReferenceDataService.getCountries()(any(), any())).thenReturn(
        Future.successful(countries)
      )

      val userAnswers = emptyUserAnswers.set(VehicleRegistrationCountryPage, country).success.value
      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, vehicleRegistrationCountryRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])
      val result         = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> "GB"))

      val expectedJson = Json.obj(
        "form"      -> filledForm,
        "mrn"       -> mrn,
        "mode"      -> NormalMode,
        "countries" -> countriesJson(true)
      )
      templateCaptor.getValue mustEqual "vehicleRegistrationCountry.njk"
      jsonCaptor.getValue must containJson(expectedJson)
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
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockReferenceDataService.getCountries()(any(), any())).thenReturn(Future.successful(countries))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(POST, vehicleRegistrationCountryRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"      -> boundForm,
        "mrn"       -> mrn,
        "mode"      -> NormalMode,
        "countries" -> countriesJson()
      )

      templateCaptor.getValue mustEqual "vehicleRegistrationCountry.njk"
      jsonCaptor.getValue must containJson(expectedJson)
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
