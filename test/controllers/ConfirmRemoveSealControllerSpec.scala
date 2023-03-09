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
import forms.ConfirmRemoveSealFormProvider
import models.{Index, NormalMode, Seal}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SealPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.p5.ConfirmRemoveSealView

import scala.concurrent.Future

class ConfirmRemoveSealControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider                = new ConfirmRemoveSealFormProvider()
  private val form                        = formProvider("seal 1")
  private val index: Index                = Index(0)
  private val mode                        = NormalMode
  private val seal                        = Seal("seal 1", removable = true)
  private lazy val confirmRemoveSealRoute = controllers.p5.routes.ConfirmRemoveSealController.onPageLoad(arrivalId, index, mode).url

  "ConfirmRemoveSeal Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(SealPage(index), seal)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, confirmRemoveSealRoute)

      val result = route(app, request).value

      status(result) mustEqual OK
      val view = injector.instanceOf[ConfirmRemoveSealView]

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, index, seal.sealId, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers.setValue(SealPage(index), seal)

      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, confirmRemoveSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(SealPage(index), seal)

      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, confirmRemoveSealRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[ConfirmRemoveSealView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, index, seal.sealId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, confirmRemoveSealRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a GET if NewSealNumberPage is undefined" in {
      checkArrivalStatus()
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, confirmRemoveSealRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      checkArrivalStatus()
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, confirmRemoveSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if NewSealNumberPage is undefined" in {
      checkArrivalStatus()
      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, confirmRemoveSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
