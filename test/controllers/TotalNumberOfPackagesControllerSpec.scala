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
import generators.Generators
import models.NormalMode
import models.P5.ArrivalMessageType.UnloadingPermission
import models.P5.{ArrivalMessageType, MessageMetaData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.TotalNumberOfPackagesPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.TotalNumberOfPackagesView
import org.scalacheck.Arbitrary.arbitrary

import java.time.LocalDateTime
import scala.concurrent.Future

class TotalNumberOfPackagesControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider: TotalNumberOfPackagesFormProvider = new TotalNumberOfPackagesFormProvider()
  private val form                                            = formProvider(index)
  private val mode                                            = NormalMode
  private val validAnswer                                     = "1"
  lazy val totalNumberOfPackagesRoute: String                 = controllers.routes.TotalNumberOfPackagesController.onPageLoad(arrivalId, index, NormalMode).url

  "TotalNumberOfPackages Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, totalNumberOfPackagesRoute)
      val result  = route(app, request).value
      val view    = injector.instanceOf[TotalNumberOfPackagesView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, arrivalId, mrn, index, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(TotalNumberOfPackagesPage, validAnswer)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, totalNumberOfPackagesRoute)
      val result  = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> validAnswer))
      val view       = injector.instanceOf[TotalNumberOfPackagesView]

      contentAsString(result) mustEqual
        view(filledForm, arrivalId, mrn, index, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, totalNumberOfPackagesRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, totalNumberOfPackagesRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result    = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[TotalNumberOfPackagesView]

      contentAsString(result) mustEqual
        view(boundForm, arrivalId, mrn, index, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, totalNumberOfPackagesRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, totalNumberOfPackagesRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
    "return OK and the correct view for a GET when message is not Unloading Permission(IE043)" in {
      checkArrivalStatus()
      val messageType = arbitrary[ArrivalMessageType].retryUntil(_ != UnloadingPermission).sample.value
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), messageType, ""))))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.TotalNumberOfPackagesController.onPageLoad(arrivalId, index, NormalMode).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId).url

    }
  }
}
