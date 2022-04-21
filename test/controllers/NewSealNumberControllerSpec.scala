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
import forms.NewSealNumberFormProvider
import models.{Index, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.NewSealNumberPage
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.NewSealNumberView

import scala.concurrent.Future

class NewSealNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new NewSealNumberFormProvider()
  private val form         = formProvider()
  private val index        = Index(0)
  private val mode         = NormalMode

  private lazy val newSealNumberRoute = routes.NewSealNumberController.onPageLoad(arrivalId, index, mode).url

  "NewSealNumber Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, newSealNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[NewSealNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.set(NewSealNumberPage(index), "answer").success.value

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, newSealNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "answer"))

      val view = injector.instanceOf[NewSealNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "onSubmit" - {
      "must redirect to the next page when valid data is submitted" in {
        checkArrivalStatus()
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers.set(NewSealNumberPage(index), "answer").success.value
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, newSealNumberRoute)
          .withFormUrlEncodedBody(("value", "answer"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        checkArrivalStatus()

        val userAnswers = emptyUserAnswers.set(NewSealNumberPage(index), "answer").success.value
        setExistingUserAnswers(userAnswers)

        val request   = FakeRequest(POST, newSealNumberRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        val view = injector.instanceOf[NewSealNumberView]

        contentAsString(result) mustEqual
          view(boundForm, mrn, arrivalId, index, mode)(request, messages).toString
      }

      "must redirect to Session Expired for a GET if no existing data is found" in {
        checkArrivalStatus()
        setNoExistingUserAnswers()

        val request = FakeRequest(GET, newSealNumberRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }

      "must redirect to Session Expired for a POST if no existing data is found" in {
        checkArrivalStatus()
        setNoExistingUserAnswers()

        val request = FakeRequest(POST, newSealNumberRoute)
          .withFormUrlEncodedBody(("value", "answer"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }

      "must redirect to the correct page when seals already in the UserAnswers" in {
        checkArrivalStatus()
        val userAnswers = UserAnswers(arrivalId, mrn, eoriNumber, Json.obj("seals" -> Seq("Seals01")))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        setExistingUserAnswers(userAnswers)

        val request =
          FakeRequest(POST, routes.NewSealNumberController.onPageLoad(arrivalId, Index(1), NormalMode).url)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
