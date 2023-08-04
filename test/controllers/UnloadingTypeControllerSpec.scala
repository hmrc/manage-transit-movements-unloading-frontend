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
import forms.EnumerableFormProvider
import generators.Generators
import matchers.JsonMatchers
import models.P5.ArrivalMessageType.UnloadingPermission
import models.P5.{ArrivalMessageType, MessageMetaData}
import models.{NormalMode, UnloadingType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary
import pages.UnloadingTypePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.UnloadingTypeView

import java.time.LocalDateTime
import scala.concurrent.Future
import org.scalacheck.Arbitrary.arbitrary

class UnloadingTypeControllerSpec extends SpecBase with Generators with AppWithDefaultMockFixtures with JsonMatchers {

  private val formProvider = new EnumerableFormProvider()
  private val form         = formProvider[UnloadingType]("unloadingType")
  private val mode         = NormalMode

  lazy val unloadingTypeRoute: String = controllers.routes.UnloadingTypeController.onPageLoad(arrivalId, mode).url

  "AddUnloadingCommentsYesNoController" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, unloadingTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[UnloadingTypeView]

      contentAsString(result) mustEqual
        view(form, mrn, UnloadingType.values, arrivalId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      val userAnswers = emptyUserAnswers.setValue(UnloadingTypePage, UnloadingType.values.head)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, unloadingTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> UnloadingType.values.head.toString))

      val view = injector.instanceOf[UnloadingTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, UnloadingType.values, arrivalId, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, unloadingTypeRoute)
          .withFormUrlEncodedBody(("value", UnloadingType.values.head.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, unloadingTypeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[UnloadingTypeView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, UnloadingType.values, arrivalId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, unloadingTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, unloadingTypeRoute)
          .withFormUrlEncodedBody(("value", UnloadingType.values.head.toString))

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

      val request = FakeRequest(GET, routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId).url

    }
  }
}
