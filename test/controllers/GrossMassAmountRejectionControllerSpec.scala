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
import forms.GrossWeightAmountFormProvider
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.GrossWeightPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.GrossWeightAmountRejectionView

import scala.concurrent.Future

class GrossWeightAmountRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new GrossWeightAmountFormProvider()
  private val form         = formProvider()

  private lazy val GrossWeightAmountRejectionRoute = routes.GrossWeightAmountRejectionController.onPageLoad(arrivalId).url

  "GrossWeightAmountRejection Controller" - {

    "must populate the view correctly on a GET" in {
      checkArrivalStatus()
      val originalValue = "100000.123"

      setExistingUserAnswers(emptyUserAnswers.setValue(GrossWeightPage, originalValue))

      val request = FakeRequest(GET, GrossWeightAmountRejectionRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[GrossWeightAmountRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustBe
        view(form.fill(originalValue), arrivalId)(request, messages).toString
    }

    "must redirect to session expired when gross mass amount is not in user answers" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, GrossWeightAmountRejectionRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val newValue = "123456.123"

      val request = FakeRequest(POST, GrossWeightAmountRejectionRoute)
        .withFormUrlEncodedBody(("value", newValue))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.get(GrossWeightPage).get mustBe newValue
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, GrossWeightAmountRejectionRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      val view = app.injector.instanceOf[GrossWeightAmountRejectionView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustBe
        view(boundForm, arrivalId)(request, messages).toString
    }
  }
}
