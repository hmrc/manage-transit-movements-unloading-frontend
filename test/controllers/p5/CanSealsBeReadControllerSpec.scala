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

package controllers.p5

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.CanSealsBeReadFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.CanSealsBeReadPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.p5.CanSealsBeReadView

import scala.concurrent.Future

class CanSealsBeReadControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new CanSealsBeReadFormProvider()
  private val form         = formProvider()
  private val mode         = NormalMode

  private lazy val canSealsBeReadRoute = routes.CanSealsBeReadController.onPageLoad(arrivalId, NormalMode).url

  "CanSealsBeRead Controller" - {

    "must return OK and the correct view for a GET" in {

      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, canSealsBeReadRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[CanSealsBeReadView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, mrn, arrivalId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, canSealsBeReadRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[CanSealsBeReadView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(filledForm, mrn, arrivalId, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, canSealsBeReadRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, canSealsBeReadRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      val view = injector.instanceOf[CanSealsBeReadView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual view(boundForm, mrn, arrivalId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, canSealsBeReadRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      checkArrivalStatus()
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, canSealsBeReadRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
