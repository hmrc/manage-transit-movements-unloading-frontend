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

package controllers.departureMeansOfTransport

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.VehicleIdentificationNumberFormProvider
import generators.Generators
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.departureMeansOfTransport.VehicleIdentificationNumberPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.departureMeansOfTransport.VehicleIdentificationNumberView

import scala.concurrent.Future

class VehicleIdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new VehicleIdentificationNumberFormProvider()
  private val form         = formProvider()
  private val mode         = NormalMode

  lazy val vehicleIdentificationNumberRoute: String =
    controllers.departureMeansOfTransport.routes.VehicleIdentificationNumberController.onPageLoad(arrivalId, index, mode).url

  "VehicleIdentificationNumber Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, vehicleIdentificationNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[VehicleIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(VehicleIdentificationNumberPage(index), "answer")
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, vehicleIdentificationNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "answer"))

      val view = injector.instanceOf[VehicleIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, vehicleIdentificationNumberRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, vehicleIdentificationNumberRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[VehicleIdentificationNumberView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, vehicleIdentificationNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, vehicleIdentificationNumberRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

  }
}