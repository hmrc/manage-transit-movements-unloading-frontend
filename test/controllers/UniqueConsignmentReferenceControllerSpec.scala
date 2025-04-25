/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.UniqueConsignmentReferenceFormProvider
import generators.Generators
import models.NormalMode
import navigation.Navigation
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.UniqueConsignmentReferencePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewModels.UniqueConsignmentReferenceViewModel
import viewModels.UniqueConsignmentReferenceViewModel.UniqueConsignmentReferenceViewModelProvider
import views.html.UniqueConsignmentReferenceView

import scala.concurrent.Future

class UniqueConsignmentReferenceControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider                                   = new UniqueConsignmentReferenceFormProvider()
  private val viewModel: UniqueConsignmentReferenceViewModel = arbitrary[UniqueConsignmentReferenceViewModel].sample.value
  private val mockViewModelProvider                          = mock[UniqueConsignmentReferenceViewModelProvider]
  private val mode                                           = NormalMode
  private val form                                           = formProvider("uniqueConsignmentReference", mode)
  private val validAnswer                                    = "ucr123"

  lazy val ucrRoute: String = routes.UniqueConsignmentReferenceController.onPageLoad(arrivalId, mode).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
    when(mockViewModelProvider.apply(any())(any())).thenReturn(viewModel)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure("feature-flags.phase-6-enabled" -> true)
      .overrides(
        bind[Navigation].toInstance(fakeNavigation),
        bind[UniqueConsignmentReferenceViewModelProvider].toInstance(mockViewModelProvider)
      )

  "uniqueConsignmentReferenceController" - {
    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, ucrRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[UniqueConsignmentReferenceView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, mode, viewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(UniqueConsignmentReferencePage, validAnswer)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, ucrRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> validAnswer))

      val view = injector.instanceOf[UniqueConsignmentReferenceView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, mode, viewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, ucrRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, ucrRoute).withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[UniqueConsignmentReferenceView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, mode, viewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, ucrRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to page not found for a GET if phase 6 is disabled" in {
      val app = super
        .guiceApplicationBuilder()
        .configure("feature-flags.phase-6-enabled" -> false)
        .build()

      running(app) {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, ucrRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.ErrorController.notFound().url
      }
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, ucrRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

  }

}
