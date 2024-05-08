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

package controllers.houseConsignment.index.documents

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.ReferenceNumberFormProvider
import generators.Generators
import models.NormalMode
import navigation.houseConsignment.index.HouseConsignmentDocumentNavigator.HouseConsignmentDocumentNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.documents.DocumentReferenceNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import viewModels.houseConsignment.index.documents.ReferenceNumberViewModel
import viewModels.houseConsignment.index.documents.ReferenceNumberViewModel.ReferenceNumberViewModelProvider
import views.html.houseConsignment.index.documents.ReferenceNumberView

import scala.concurrent.Future

class ReferenceNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val viewModel    = arbitrary[ReferenceNumberViewModel].sample.value
  private val formProvider = new ReferenceNumberFormProvider()

  private val houseConsignmentMode = NormalMode
  private val documentMode         = NormalMode

  private val form                  = formProvider(viewModel.requiredError, hcIndex, Seq.empty)
  private val mockViewModelProvider = mock[ReferenceNumberViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[HouseConsignmentDocumentNavigatorProvider].toInstance(FakeHouseConsignmentNavigators.fakeDocumentNavigatorProvider),
        bind[ReferenceNumberViewModelProvider].toInstance(mockViewModelProvider)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
    when(mockViewModelProvider.apply(any(), any())(any()))
      .thenReturn(viewModel)

  }

  private lazy val referenceNumberRoute =
    routes.ReferenceNumberController.onPageLoad(arrivalId, houseConsignmentMode, documentMode, hcIndex, documentIndex).url

  "ReferenceNumberController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, referenceNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[ReferenceNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, houseConsignmentMode, documentMode, viewModel, houseConsignmentIndex, documentIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(DocumentReferenceNumberPage(hcIndex, documentIndex), "testString")
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, referenceNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "testString"))

      val view = injector.instanceOf[ReferenceNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentMode, documentMode, viewModel, hcIndex, documentIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, referenceNumberRoute)
        .withFormUrlEncodedBody(("value", "testString"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, referenceNumberRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[ReferenceNumberView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentMode, documentMode, viewModel, hcIndex, documentIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, referenceNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, referenceNumberRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
