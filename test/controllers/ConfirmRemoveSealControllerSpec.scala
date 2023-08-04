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
import generators.Generators
import models.NormalMode
import models.P5.ArrivalMessageType.UnloadingPermission
import models.P5.{ArrivalMessageType, MessageMetaData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.NewSealPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ConfirmRemoveSealView
import org.scalacheck.Arbitrary.arbitrary

import java.time.LocalDateTime
import scala.concurrent.Future

class ConfirmRemoveSealControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider                = new ConfirmRemoveSealFormProvider()
  private val form                        = formProvider("seal 1")
  private val mode                        = NormalMode
  private val seal                        = "seal 1"
  private lazy val confirmRemoveSealRoute = controllers.routes.ConfirmRemoveSealController.onPageLoad(arrivalId, equipmentIndex, sealIndex, mode).url

  "ConfirmRemoveSeal Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      val userAnswers = emptyUserAnswers.setValue(NewSealPage(equipmentIndex, sealIndex), seal)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, confirmRemoveSealRoute)

      val result = route(app, request).value

      status(result) mustEqual OK
      val view = injector.instanceOf[ConfirmRemoveSealView]

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, equipmentIndex, sealIndex, seal, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers.setValue(NewSealPage(equipmentIndex, sealIndex), seal)

      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, confirmRemoveSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      val userAnswers = emptyUserAnswers.setValue(NewSealPage(equipmentIndex, sealIndex), seal)

      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, confirmRemoveSealRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[ConfirmRemoveSealView]
      println("\n\n\nContent\n\n" + contentAsString(result))
      println("\n\n\nMust equal\n\n" + view(boundForm, mrn, arrivalId, equipmentIndex, sealIndex, seal, mode)(request, messages).toString)
      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, equipmentIndex, sealIndex, seal, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, confirmRemoveSealRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a GET if NewSealNumberPage is undefined" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, confirmRemoveSealRoute)

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
        FakeRequest(POST, confirmRemoveSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if NewSealNumberPage is undefined" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, confirmRemoveSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

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

      val request = FakeRequest(GET, routes.ConfirmRemoveSealController.onPageLoad(arrivalId, index, index, NormalMode).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId).url

    }
  }
}
