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

package controllers.houseConsignment.index.additionalReference

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.HouseConsignmentAdditionalReferenceNumberFormProvider
import generators.Generators
import models.NormalMode
import navigation.houseConsignment.index.AdditionalReferenceNavigator.AdditionalReferenceNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.houseConsignment.index.additionalReference.HouseConsignmentAdditionalReferenceNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.additionalReference.AdditionalReferenceNumberView

import scala.concurrent.Future

class AdditionalReferenceNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private lazy val formProvider = new HouseConsignmentAdditionalReferenceNumberFormProvider()
  private lazy val form         = formProvider("houseConsignment.index.additionalReference.additionalReferenceNumber")

  private val houseConsignmentMode    = NormalMode
  private val additionalReferenceMode = NormalMode

  private lazy val additionalReferenceNumberRoute =
    routes.AdditionalReferenceNumberController
      .onPageLoad(arrivalId, houseConsignmentMode, additionalReferenceMode, houseConsignmentIndex, additionalReferenceIndex)
      .url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[AdditionalReferenceNavigatorProvider].toInstance(FakeHouseConsignmentNavigators.fakeAdditionalReferenceNavigatorProvider)
      )

  "AdditionalReferenceNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, additionalReferenceNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[AdditionalReferenceNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, arrivalId, mrn, houseConsignmentMode, additionalReferenceMode, houseConsignmentIndex, additionalReferenceIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, additionalReferenceIndex), "test string")
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, additionalReferenceNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "test string"))

      val view = injector.instanceOf[AdditionalReferenceNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, arrivalId, mrn, houseConsignmentMode, additionalReferenceMode, houseConsignmentIndex, additionalReferenceIndex)(request,
                                                                                                                                         messages
        ).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, additionalReferenceNumberRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, additionalReferenceNumberRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[AdditionalReferenceNumberView]

      contentAsString(result) mustEqual
        view(filledForm, arrivalId, mrn, houseConsignmentMode, additionalReferenceMode, houseConsignmentIndex, additionalReferenceIndex)(request,
                                                                                                                                         messages
        ).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, additionalReferenceNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, additionalReferenceNumberRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
