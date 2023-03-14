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
import forms.NewSealNumberFormProvider
import models.{NormalMode, Seal, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.SealPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.NewSealNumberView

import scala.concurrent.Future

class NewSealNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new NewSealNumberFormProvider()
  private val form         = formProvider()
  private val mode         = NormalMode

  private val validAnswer = "seal ID"

  private lazy val newSealNumberRoute = controllers.routes.NewSealNumberController.onPageLoad(arrivalId, equipmentIndex, sealIndex, mode).url

  "NewSealNumber Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, newSealNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[NewSealNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, equipmentIndex, sealIndex, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(SealPage(equipmentIndex, sealIndex), validAnswer)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, newSealNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> validAnswer))

      val view = injector.instanceOf[NewSealNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, equipmentIndex, sealIndex, mode)(request, messages).toString
    }

    "onSubmit" - {
      "must redirect to the next page when valid data is submitted" - {
        "adding a new seal" in {
          checkArrivalStatus()
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, newSealNumberRoute)
            .withFormUrlEncodedBody(("value", validAnswer))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswersCaptor.capture())
          userAnswersCaptor.getValue.get(SealPage(equipmentIndex, sealIndex)).get mustBe validAnswer
        }

        "updating a new seal" in {
          checkArrivalStatus()
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = emptyUserAnswers.setValue(SealPage(equipmentIndex, sealIndex), "value before update")
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, newSealNumberRoute)
            .withFormUrlEncodedBody(("value", validAnswer))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswersCaptor.capture())
          userAnswersCaptor.getValue.get(SealPage(equipmentIndex, sealIndex)).get mustBe validAnswer
        }

        "updating an existing seal" in {
          checkArrivalStatus()
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = emptyUserAnswers.setValue(SealPage(equipmentIndex, sealIndex), "value before update")
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, newSealNumberRoute)
            .withFormUrlEncodedBody(("value", validAnswer))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswersCaptor.capture())
          userAnswersCaptor.getValue.get(SealPage(equipmentIndex, sealIndex)).get mustBe validAnswer
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val invalidAnswer = ""
        checkArrivalStatus()

        setExistingUserAnswers(emptyUserAnswers)

        val request   = FakeRequest(POST, newSealNumberRoute).withFormUrlEncodedBody(("value", invalidAnswer))
        val boundForm = form.bind(Map("value" -> invalidAnswer))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        val view = injector.instanceOf[NewSealNumberView]

        contentAsString(result) mustEqual
          view(boundForm, mrn, arrivalId, equipmentIndex, sealIndex, mode)(request, messages).toString
      }
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
  }
}
