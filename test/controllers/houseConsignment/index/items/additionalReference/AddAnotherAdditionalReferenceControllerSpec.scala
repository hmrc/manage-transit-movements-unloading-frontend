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

package controllers.houseConsignment.index.items.additionalReference

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.AddAnotherFormProvider
import generators.Generators
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferencePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewModels.ListItem
import viewModels.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferenceViewModel
import viewModels.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferenceViewModel.AddAnotherAdditionalReferenceViewModelProvider
import views.html.houseConsignment.index.items.additionalReference.AddAnotherAdditionalReferenceView

class AddAnotherAdditionalReferenceControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherAdditionalReferenceViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore)

  private val houseConsignmentMode = NormalMode
  private val itemMode             = NormalMode

  private lazy val addAnotherAdditionalReferenceRoute =
    routes.AddAnotherAdditionalReferenceController
      .onPageLoad(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
      .url

  private val mockViewModelProvider = mock[AddAnotherAdditionalReferenceViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AddAnotherAdditionalReferenceViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxAdditionalReferences - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxAdditionalReferences)(listItem)

  private val viewModel = arbitrary[AddAnotherAdditionalReferenceViewModel].sample.value

  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherAdditionalReferenceController" - {
    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), ArgumentMatchers.eq(itemIndex)))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherAdditionalReferenceRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherAdditionalReferenceView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), ArgumentMatchers.eq(itemIndex)))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherAdditionalReferenceRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherAdditionalReferenceView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), mrn, arrivalId, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), ArgumentMatchers.eq(itemIndex)))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherAdditionalReferencePage(houseConsignmentIndex, itemIndex), true))

        val request = FakeRequest(GET, addAnotherAdditionalReferenceRoute)

        val result = route(app, request).value

        val filledForm = form(notMaxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherAdditionalReferenceView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), ArgumentMatchers.eq(itemIndex)))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherAdditionalReferenceRoute)

        val result = route(app, request).value

        val filledForm = form(maxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherAdditionalReferenceView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, mrn, arrivalId, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to additional reference type page at next index" in {
          when(mockViewModelProvider.apply(any(), any(), any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), ArgumentMatchers.eq(itemIndex)))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherAdditionalReferenceRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.AdditionalReferenceTypeController
            .onPageLoad(arrivalId, houseConsignmentMode, itemMode, NormalMode, houseConsignmentIndex, itemIndex, notMaxedOutViewModel.nextIndex)
            .url

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswersCaptor.capture())
          userAnswersCaptor.getValue.getValue(AddAnotherAdditionalReferencePage(houseConsignmentIndex, itemIndex)) mustEqual true
        }
      }

      "when no submitted" - {
        "must redirect to next page" - {
          "when normal mode" in {
            val addAnotherAdditionalReferenceRoute =
              routes.AddAnotherAdditionalReferenceController
                .onPageLoad(arrivalId, CheckMode, CheckMode, houseConsignmentIndex, itemIndex)
                .url

            when(mockViewModelProvider.apply(any(), any(), any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), ArgumentMatchers.eq(itemIndex)))
              .thenReturn(notMaxedOutViewModel)

            setExistingUserAnswers(emptyUserAnswers)

            val request = FakeRequest(POST, addAnotherAdditionalReferenceRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.getValue(AddAnotherAdditionalReferencePage(houseConsignmentIndex, itemIndex)) mustEqual false
          }

          "when check mode" in {
            when(mockViewModelProvider.apply(any(), any(), any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), ArgumentMatchers.eq(itemIndex)))
              .thenReturn(notMaxedOutViewModel)

            setExistingUserAnswers(emptyUserAnswers)

            val request = FakeRequest(POST, addAnotherAdditionalReferenceRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual controllers.houseConsignment.index.items.routes.AddPackagesYesNoController
              .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
              .url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.getValue(AddAnotherAdditionalReferencePage(houseConsignmentIndex, itemIndex)) mustEqual false
          }
        }
      }
    }

    "when max limit reached" - {
      "must redirect to next page" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), ArgumentMatchers.eq(itemIndex)))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherAdditionalReferenceRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.houseConsignment.index.items.routes.AddPackagesYesNoController
          .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, houseConsignmentMode, itemMode)
          .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.getValue(AddAnotherAdditionalReferencePage(houseConsignmentIndex, itemIndex)) mustEqual false
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any(), ArgumentMatchers.eq(houseConsignmentIndex), ArgumentMatchers.eq(itemIndex)))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherAdditionalReferenceRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider(notMaxedOutViewModel.prefix, notMaxedOutViewModel.allowMore, houseConsignmentIndex, itemIndex)
          .bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherAdditionalReferenceView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherAdditionalReferenceRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherAdditionalReferenceRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
