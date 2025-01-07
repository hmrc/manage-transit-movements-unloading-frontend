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
import models.{NormalMode, UnloadingType}
import navigation.Navigation
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.UnloadingTypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.UnloadingTypeView

import scala.concurrent.Future

class UnloadingTypeControllerSpec extends SpecBase with Generators with AppWithDefaultMockFixtures with JsonMatchers {

  private val mode         = NormalMode
  private val formProvider = new EnumerableFormProvider()
  private val form         = formProvider[UnloadingType](mode, "unloadingType")

  lazy val unloadingTypeRoute: String = controllers.routes.UnloadingTypeController.onPageLoad(arrivalId, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[Navigation].toInstance(fakeNavigation)
      )

  "UnloadingTypeController" - {

    "must return OK and the correct view for a GET" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, unloadingTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[UnloadingTypeView]

      contentAsString(result) mustEqual
        view(form, mrn, UnloadingType.values, arrivalId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
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
      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, unloadingTypeRoute)
          .withFormUrlEncodedBody(("value", UnloadingType.values.head.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
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
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, unloadingTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, unloadingTypeRoute)
          .withFormUrlEncodedBody(("value", UnloadingType.values.head.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

  }
}
