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
import forms.TotalNumberOfItemsFormProvider
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.TotalNumberOfItemsPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.TotalNumberOfItemsRejectionView

import scala.concurrent.Future

class TotalNumberOfItemsRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider                 = new TotalNumberOfItemsFormProvider()
  private val form                         = formProvider()
  private val validAnswer                  = 1
  lazy val totalNumberOfItemsRoute: String = routes.TotalNumberOfItemsRejectionController.onPageLoad(arrivalId).url

  "TotalNumberOfItems Rejection Controller" - {

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers.setValue(TotalNumberOfItemsPage, validAnswer))

      val request = FakeRequest(GET, totalNumberOfItemsRoute)
      val view    = injector.instanceOf[TotalNumberOfItemsRejectionView]
      val result  = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> validAnswer.toString))

      contentAsString(result) mustEqual
        view(filledForm, arrivalId)(request, messages).toString
    }

    "must redirect to session expired when total number of items is not in user answers" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, totalNumberOfItemsRoute)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, totalNumberOfItemsRoute)
        .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.get(TotalNumberOfItemsPage).get mustBe validAnswer
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, totalNumberOfItemsRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result    = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[TotalNumberOfItemsRejectionView]

      contentAsString(result) mustEqual view(boundForm, arrivalId)(request, messages).toString
    }
  }
}
