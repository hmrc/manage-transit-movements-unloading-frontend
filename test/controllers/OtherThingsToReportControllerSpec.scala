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
import forms.Constants.otherThingsToReportLength
import forms.OtherThingsToReportFormProvider
import generators.Generators
import models.NormalMode
import navigation.Navigation
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.{NewAuthYesNoPage, OtherThingsToReportPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.OtherThingsToReportViewModel
import viewModels.OtherThingsToReportViewModel.OtherThingsToReportViewModelProvider
import views.html.OtherThingsToReportView

import scala.concurrent.Future

class OtherThingsToReportControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val viewModel = arbitrary[OtherThingsToReportViewModel].sample.value

  private val formProvider = new OtherThingsToReportFormProvider()
  private val form         = formProvider(viewModel.requiredError, viewModel.maxLengthError, viewModel.invalidError)
  private val mode         = NormalMode

  private lazy val otherThingsToReportRoute = controllers.routes.OtherThingsToReportController.onPageLoad(arrivalId, NormalMode).url

  private val mockViewModelProvider = mock[OtherThingsToReportViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[Navigation].toInstance(fakeNavigation),
        bind(classOf[OtherThingsToReportViewModelProvider]).toInstance(mockViewModelProvider)
      )

  private val newAuth = arbitrary[Boolean].sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
      .thenReturn(viewModel)
  }

  "OtherThingsToReportController" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(NewAuthYesNoPage, newAuth)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, otherThingsToReportRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[OtherThingsToReportView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, mrn, arrivalId, otherThingsToReportLength, mode, viewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers
        .setValue(NewAuthYesNoPage, newAuth)
        .setValue(OtherThingsToReportPage, "answer")
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, otherThingsToReportRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "answer"))

      val view = injector.instanceOf[OtherThingsToReportView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(filledForm, mrn, arrivalId, otherThingsToReportLength, mode, viewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val userAnswers = emptyUserAnswers.setValue(NewAuthYesNoPage, newAuth)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, otherThingsToReportRoute)
        .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(NewAuthYesNoPage, false)

      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, otherThingsToReportRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      val view = injector.instanceOf[OtherThingsToReportView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual view(boundForm, mrn, arrivalId, otherThingsToReportLength, mode, viewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, otherThingsToReportRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, otherThingsToReportRoute)
        .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

  }
}
