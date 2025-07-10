/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.houseConsignment.index.items

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import models.NormalMode
import navigation.FakeHouseConsignmentItemNavigatorProviderProvider
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator.HouseConsignmentItemNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.houseConsignment.index.items.AddDocumentYesNoPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.houseConsignment.index.items.AddDocumentYesNoView

import scala.concurrent.Future

class AddDocumentYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("houseConsignment.item.addDocumentYesNo")

  private val houseConsignmentMode = NormalMode
  private val itemMode             = NormalMode

  private lazy val addDocumentYesNoRoute =
    routes.AddDocumentYesNoController
      .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
      .url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[HouseConsignmentItemNavigatorProvider].toProvider(classOf[FakeHouseConsignmentItemNavigatorProviderProvider])
      )

  "AddDocumentYesNoController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, addDocumentYesNoRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[AddDocumentYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, addDocumentYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddDocumentYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, addDocumentYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, addDocumentYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[AddDocumentYesNoView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addDocumentYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addDocumentYesNoRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
