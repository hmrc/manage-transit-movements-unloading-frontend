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
import forms.NewContainerIdentificationNumberFormProvider
import generators.Generators
import models.NormalMode
import models.P5.ArrivalMessageType.UnloadingPermission
import models.P5.{ArrivalMessageType, MessageMetaData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.ContainerIdentificationNumberPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.NewContainerIdentificationNumberView

import java.time.LocalDateTime
import scala.concurrent.Future

class NewContainerIdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new NewContainerIdentificationNumberFormProvider()
  private val form         = formProvider()
  private val mode         = NormalMode

  private val validAnswer = "container ID"

  private lazy val newContainerIdentificationNumberRoute =
    controllers.routes.NewContainerIdentificationNumberController.onPageLoad(arrivalId, index, mode).url

  "NewContainerIdentificationNumberController" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, newContainerIdentificationNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[NewContainerIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      val userAnswers = emptyUserAnswers.setValue(ContainerIdentificationNumberPage(index), validAnswer)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, newContainerIdentificationNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> validAnswer))

      val view = injector.instanceOf[NewContainerIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "onSubmit" - {
      "must redirect to the next page when valid data is submitted" - {
        "when adding a new container identification number that is not a duplicate" ignore {} // TODO: When duplicate check is implemented
      }

      "must display a form with errors" - {
        "when adding a new container identification number that is a duplicate" ignore {} // TODO: When duplicate check is implemented
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val invalidAnswer = ""
        checkArrivalStatus()
        when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
          .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))

        setExistingUserAnswers(emptyUserAnswers)

        val request   = FakeRequest(POST, newContainerIdentificationNumberRoute).withFormUrlEncodedBody(("value", invalidAnswer))
        val boundForm = form.bind(Map("value" -> invalidAnswer))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        val view = injector.instanceOf[NewContainerIdentificationNumberView]

        contentAsString(result) mustEqual
          view(boundForm, mrn, arrivalId, index, mode)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, newContainerIdentificationNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, newContainerIdentificationNumberRoute)
        .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "return OK and the correct view for a GET when message is not Unloading Permission(IE043)" in {
      checkArrivalStatus()
      val messageType = arbitrary[ArrivalMessageType].retryUntil(_ != UnloadingPermission).sample.value
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), messageType, ""))))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.NewContainerIdentificationNumberController.onPageLoad(arrivalId, index, NormalMode).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId).url

    }
  }
}
