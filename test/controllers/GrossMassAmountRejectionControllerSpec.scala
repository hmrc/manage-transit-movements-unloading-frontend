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
import forms.GrossMassAmountFormProvider
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.GrossMassAmountPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.GrossMassAmountRejectionView

import scala.concurrent.Future

class GrossMassAmountRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new GrossMassAmountFormProvider()
  private val form         = formProvider()

  private lazy val grossMassAmountRejectionRoute = routes.GrossMassAmountRejectionController.onPageLoad(arrivalId).url

  "GrossMassAmountRejection Controller" - {

    "must populate the view correctly on a GET when the value has not been extracted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, grossMassAmountRejectionRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[GrossMassAmountRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustBe
        view(form, arrivalId)(request, messages).toString
    }

    "must populate the view correctly on a GET when the value has been extracted" in {
      checkArrivalStatus()
      val originalValue = "100000.123"

      setExistingUserAnswers(emptyUserAnswers.setValue(GrossMassAmountPage, originalValue))

      val request = FakeRequest(GET, grossMassAmountRejectionRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[GrossMassAmountRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustBe
        view(form.fill(originalValue), arrivalId)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val newValue = "123456.123"

      val request = FakeRequest(POST, grossMassAmountRejectionRoute)
        .withFormUrlEncodedBody(("value", newValue))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.get(GrossMassAmountPage).get mustBe newValue
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, grossMassAmountRejectionRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      val view = app.injector.instanceOf[GrossMassAmountRejectionView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustBe
        view(boundForm, arrivalId)(request, messages).toString
    }
  }
}
