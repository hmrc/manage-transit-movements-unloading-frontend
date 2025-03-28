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

package controllers.transportEquipment.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.AddAnotherFormProvider
import generators.Generators
import models.{CheckMode, Index, Mode, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.transportEquipment.index.AddAnotherSealPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewModels.ListItem
import viewModels.transportEquipment.index.AddAnotherSealViewModel
import viewModels.transportEquipment.index.AddAnotherSealViewModel.AddAnotherSealViewModelProvider
import views.html.transportEquipment.index.AddAnotherSealView

class AddAnotherSealControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherSealViewModel, equipmentIndex: Index): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore(frontendAppConfig), equipmentIndex.display)

  private val equipmentMode = NormalMode

  private lazy val addAnotherSealRoute = routes.AddAnotherSealController.onPageLoad(arrivalId, equipmentMode, equipmentIndex).url

  private val mockViewModelProvider = mock[AddAnotherSealViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AddAnotherSealViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxSeals - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxSeals)(listItem)

  private val viewModel = arbitrary[AddAnotherSealViewModel].sample.value

  private val emptyViewModel       = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherSeal Controller" - {
    "must return OK and the correct view for a GET" - {
      "when 0 seals" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any()))
          .thenReturn(emptyViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherSealView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(emptyViewModel, equipmentIndex), mrn, arrivalId, emptyViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherSealRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherSealView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel, equipmentIndex), mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherSealRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherSealView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel, equipmentIndex), mrn, arrivalId, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherSealPage(equipmentIndex), true))

        val request = FakeRequest(GET, addAnotherSealRoute)

        val result = route(app, request).value

        val filledForm = form(notMaxedOutViewModel, equipmentIndex).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherSealView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherSealPage(equipmentIndex), true))

        val request = FakeRequest(GET, addAnotherSealRoute)

        val result = route(app, request).value

        val filledForm = form(maxedOutViewModel, equipmentIndex).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherSealView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, mrn, arrivalId, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to seal id number page at next index" in {
          forAll(arbitrary[Mode]) {
            equipmentMode =>
              beforeEach()

              lazy val addAnotherSealRoute = routes.AddAnotherSealController.onPageLoad(arrivalId, equipmentMode, equipmentIndex).url

              when(mockViewModelProvider.apply(any(), any(), any(), any()))
                .thenReturn(notMaxedOutViewModel)

              setExistingUserAnswers(emptyUserAnswers)

              val request = FakeRequest(POST, addAnotherSealRoute)
                .withFormUrlEncodedBody(("value", "true"))

              val result = route(app, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual controllers.transportEquipment.index.seals.routes.SealIdentificationNumberController
                .onPageLoad(arrivalId, equipmentMode, NormalMode, equipmentIndex, notMaxedOutViewModel.nextIndex)
                .url

              val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
              verify(mockSessionRepository).set(userAnswersCaptor.capture())
              userAnswersCaptor.getValue.get(AddAnotherSealPage(equipmentIndex)).value mustEqual true
          }
        }
      }

      "when no submitted" - {
        "must redirect to next page" - {
          "when equipment mode is CheckMode" in {
            lazy val addAnotherSealRoute = routes.AddAnotherSealController.onPageLoad(arrivalId, CheckMode, equipmentIndex).url

            when(mockViewModelProvider.apply(any(), any(), any(), any()))
              .thenReturn(notMaxedOutViewModel)

            setExistingUserAnswers(emptyUserAnswers)

            val request = FakeRequest(POST, addAnotherSealRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.getValue(AddAnotherSealPage(equipmentIndex)) mustEqual false
          }

          "when equipment mode is NormalMode" in {
            lazy val addAnotherSealRoute = routes.AddAnotherSealController.onPageLoad(arrivalId, NormalMode, equipmentIndex).url

            when(mockViewModelProvider.apply(any(), any(), any(), any()))
              .thenReturn(notMaxedOutViewModel)

            setExistingUserAnswers(emptyUserAnswers)

            val request = FakeRequest(POST, addAnotherSealRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.transportEquipment.index.routes.ApplyAnItemYesNoController.onPageLoad(arrivalId, equipmentIndex, NormalMode).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.getValue(AddAnotherSealPage(equipmentIndex)) mustEqual false
          }
        }
      }
    }

    "when max limit reached" - {
      "must redirect to next page" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherSealRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transportEquipment.index.routes.ApplyAnItemYesNoController.onPageLoad(arrivalId, equipmentIndex, equipmentMode).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.getValue(AddAnotherSealPage(equipmentIndex)) mustEqual false
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any(), any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherSealRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form(notMaxedOutViewModel, equipmentIndex).bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherSealView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, mrn, arrivalId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherSealRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherSealRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
