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
import forms.VehicleNameRegistrationReferenceFormProvider
import generators.MessagesModelGenerators
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.VehicleNameRegistrationReferencePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.VehicleNameRegistrationRejectionView

import scala.concurrent.Future

class VehicleNameRegistrationRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MessagesModelGenerators {

  private val formProvider = new VehicleNameRegistrationReferenceFormProvider()
  private val form         = formProvider()

  private lazy val vehicleNameRegistrationRejectionRoute: String = routes.VehicleNameRegistrationRejectionController.onPageLoad(arrivalId).url

  "VehicleNameRegistrationRejectionController Controller" - {

    "must populate the view correctly on a GET when the value has not been extracted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, vehicleNameRegistrationRejectionRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[VehicleNameRegistrationRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, arrivalId)(request, messages).toString
    }

    "must populate the view correctly on a GET when the value has been extracted" in {
      checkArrivalStatus()
      val originalValue = "some reference"

      setExistingUserAnswers(emptyUserAnswers.setValue(VehicleNameRegistrationReferencePage, originalValue))

      val request = FakeRequest(GET, vehicleNameRegistrationRejectionRoute)
      val result  = route(app, request).value

      val filledForm = form.bind(Map("value" -> originalValue))
      val view       = injector.instanceOf[VehicleNameRegistrationRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, arrivalId)(request, messages).toString
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, vehicleNameRegistrationRejectionRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      val view = injector.instanceOf[VehicleNameRegistrationRejectionView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, arrivalId)(request, messages).toString
    }

    "must redirect to check your answers page for a POST" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val newValue = "answer"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, vehicleNameRegistrationRejectionRoute)
        .withFormUrlEncodedBody(("value", newValue))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.get(VehicleNameRegistrationReferencePage).get mustBe newValue
    }
  }
}
