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
import forms.TotalNumberOfPackagesFormProvider
import models.{Index, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.TotalNumberOfPackagesPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.TotalNumberOfPackagesRejectionView

import scala.concurrent.Future

class TotalNumberOfPackagesRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val index = Index(0)

  private val formProvider                    = new TotalNumberOfPackagesFormProvider()
  private val form                            = formProvider(index)
  private val validAnswer                     = "1"
  private lazy val totalNumberOfPackagesRoute = routes.TotalNumberOfPackagesRejectionController.onPageLoad(arrivalId).url

  "TotalNumberOfPackages Rejection Controller" - {

    "must populate the view correctly on a GET" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers.setValue(TotalNumberOfPackagesPage, validAnswer))

      val request = FakeRequest(GET, totalNumberOfPackagesRoute)
      val view    = injector.instanceOf[TotalNumberOfPackagesRejectionView]
      val result  = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> validAnswer))

      contentAsString(result) mustEqual
        view(filledForm, arrivalId)(request, messages).toString
    }

    "must redirect to session expired when total number of packages is not in user answers" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, totalNumberOfPackagesRoute)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, totalNumberOfPackagesRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.get(TotalNumberOfPackagesPage).get mustBe validAnswer
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, totalNumberOfPackagesRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result    = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[TotalNumberOfPackagesRejectionView]

      contentAsString(result) mustEqual view(boundForm, arrivalId)(request, messages).toString
    }
  }
}
