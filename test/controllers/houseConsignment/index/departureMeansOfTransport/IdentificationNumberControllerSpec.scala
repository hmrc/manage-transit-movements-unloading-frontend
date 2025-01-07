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
import forms.VehicleIdentificationNumberFormProvider
import generators.Generators
import models.NormalMode
import navigation.houseConsignment.index.departureMeansOfTransport.DepartureTransportMeansNavigator.DepartureTransportMeansNavigatorProvider
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.departureMeansOfTransport.VehicleIdentificationNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.houseConsignment.index.departureTransportMeans.IdentificationNumberViewModel
import viewModels.houseConsignment.index.departureTransportMeans.IdentificationNumberViewModel.IdentificationNumberViewModelProvider
import views.html.houseConsignment.index.departureMeansOfTransport.IdentificationNumberView

import scala.concurrent.Future

class IdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val prefix       = "houseConsignment.index.departureMeansOfTransport.identificationNumber"
  private val formProvider = new VehicleIdentificationNumberFormProvider()

  private val houseConsignmentMode = NormalMode
  private val transportMeansMode   = NormalMode

  private val form                  = formProvider(prefix, transportMeansMode, houseConsignmentIndex)
  private val viewModel             = arbitrary[IdentificationNumberViewModel].sample.value
  private val mockViewModelProvider = mock[IdentificationNumberViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[DepartureTransportMeansNavigatorProvider]).toInstance(FakeHouseConsignmentNavigators.fakeDepartureTransportMeansNavigatorProvider),
        bind[IdentificationNumberViewModelProvider].toInstance(mockViewModelProvider)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)

    when(mockViewModelProvider.apply(any(), ArgumentMatchers.eq(houseConsignmentIndex))(any()))
      .thenReturn(viewModel)
  }

  lazy val vehicleIdentificationNumberRoute: String =
    routes.IdentificationNumberController.onPageLoad(arrivalId, houseConsignmentIndex, index, houseConsignmentMode, transportMeansMode).url

  "departureMeansOfTransport.identificationNumber Controller" - {

    "must return OK and the correct view for a GET" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, vehicleIdentificationNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[IdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, houseConsignmentIndex, index, houseConsignmentMode, transportMeansMode, viewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.setValue(VehicleIdentificationNumberPage(houseConsignmentIndex, index), "answer")
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, vehicleIdentificationNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "answer"))

      val view = injector.instanceOf[IdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentIndex, index, houseConsignmentMode, transportMeansMode, viewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, vehicleIdentificationNumberRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, vehicleIdentificationNumberRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[IdentificationNumberView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, houseConsignmentIndex, index, houseConsignmentMode, transportMeansMode, viewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, vehicleIdentificationNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, vehicleIdentificationNumberRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

  }
}
