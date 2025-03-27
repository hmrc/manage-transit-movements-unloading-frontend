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

package controllers.houseConsignment.index.departureMeansOfTransport

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.houseConsignment.index.departureMeansOfTransport.routes as departureMeansOfTransportRoutes
import controllers.routes
import forms.AddAnotherFormProvider
import generators.Generators
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.houseConsignment.index.departureMeansOfTransport.AddAnotherDepartureMeansOfTransportPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewModels.ListItem
import viewModels.houseConsignment.index.departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel
import viewModels.houseConsignment.index.departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel.AddAnotherDepartureMeansOfTransportViewModelProvider
import views.html.houseConsignment.index.departureMeansOfTransport.AddAnotherDepartureMeansOfTransportView

class AddAnotherDepartureMeansOfTransportControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherDepartureMeansOfTransportViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore)

  private val houseConsignmentMode = NormalMode

  private lazy val addAnotherDepartureMeansOfTransportRoute =
    departureMeansOfTransportRoutes.AddAnotherDepartureMeansOfTransportController.onPageLoad(arrivalId, houseConsignmentIndex, houseConsignmentMode).url

  private val mockViewModelProvider = mock[AddAnotherDepartureMeansOfTransportViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AddAnotherDepartureMeansOfTransportViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxDepartureMeansOfTransportHouseConsignment - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxDepartureMeansOfTransportHouseConsignment)(listItem)

  private val viewModel = arbitrary[AddAnotherDepartureMeansOfTransportViewModel].sample.value

  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherDepartureMeansOfTransportController" - {
    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherDepartureMeansOfTransportRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherDepartureMeansOfTransportView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherDepartureMeansOfTransportRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherDepartureMeansOfTransportView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), mrn, arrivalId, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherDepartureMeansOfTransportPage(houseConsignmentIndex), true))

        val request = FakeRequest(GET, addAnotherDepartureMeansOfTransportRoute)

        val result = route(app, request).value

        val filledForm = form(notMaxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherDepartureMeansOfTransportView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherDepartureMeansOfTransportPage(houseConsignmentIndex), true))

        val request = FakeRequest(GET, addAnotherDepartureMeansOfTransportRoute)

        val result = route(app, request).value

        val filledForm = form(maxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherDepartureMeansOfTransportView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, mrn, arrivalId, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to add identification yes no page at next index" in {
          when(mockViewModelProvider.apply(any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherDepartureMeansOfTransportRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual departureMeansOfTransportRoutes.IdentificationController
            .onPageLoad(arrivalId, houseConsignmentIndex, notMaxedOutViewModel.nextIndex, houseConsignmentMode, NormalMode)
            .url

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswersCaptor.capture())
          userAnswersCaptor.getValue.getValue(AddAnotherDepartureMeansOfTransportPage(houseConsignmentIndex)) mustEqual true
        }
      }

      "when no submitted" - {
        "must redirect to next page" - {
          "when house consignment mode is CheckMode" in {
            lazy val addAnotherDepartureMeansOfTransportRoute = departureMeansOfTransportRoutes.AddAnotherDepartureMeansOfTransportController
              .onPageLoad(arrivalId, houseConsignmentIndex, CheckMode)
              .url

            when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
              .thenReturn(notMaxedOutViewModel)

            setExistingUserAnswers(emptyUserAnswers)

            val request = FakeRequest(POST, addAnotherDepartureMeansOfTransportRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.getValue(AddAnotherDepartureMeansOfTransportPage(houseConsignmentIndex)) mustEqual false
          }

          "when house consignment mode is NormalMode" in {
            lazy val addAnotherDepartureMeansOfTransportRoute = departureMeansOfTransportRoutes.AddAnotherDepartureMeansOfTransportController
              .onPageLoad(arrivalId, houseConsignmentIndex, NormalMode)
              .url

            when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
              .thenReturn(notMaxedOutViewModel)

            setExistingUserAnswers(emptyUserAnswers)

            val request = FakeRequest(POST, addAnotherDepartureMeansOfTransportRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.houseConsignment.index.routes.AddDocumentsYesNoController.onPageLoad(arrivalId, NormalMode, houseConsignmentIndex).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.getValue(AddAnotherDepartureMeansOfTransportPage(houseConsignmentIndex)) mustEqual false
          }
        }
      }
    }

    "when max limit reached" - {
      "must redirect to next page" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherDepartureMeansOfTransportRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.houseConsignment.index.routes.AddDocumentsYesNoController
          .onPageLoad(arrivalId, NormalMode, houseConsignmentIndex)
          .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.getValue(AddAnotherDepartureMeansOfTransportPage(houseConsignmentIndex)) mustEqual false
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherDepartureMeansOfTransportRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider(notMaxedOutViewModel.prefix, notMaxedOutViewModel.allowMore, houseConsignmentIndex)
          .bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherDepartureMeansOfTransportView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherDepartureMeansOfTransportRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherDepartureMeansOfTransportRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
