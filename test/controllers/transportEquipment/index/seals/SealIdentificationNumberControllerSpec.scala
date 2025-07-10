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

package controllers.transportEquipment.index.seals

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.SealIdentificationNumberFormProvider
import generators.Generators
import models.NormalMode
import navigation.SealNavigator.SealNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import viewModels.transportEquipment.index.seals.SealIdentificationNumberViewModel
import viewModels.transportEquipment.index.seals.SealIdentificationNumberViewModel.SealIdentificationNumberViewModelProvider
import views.html.transportEquipment.index.seals.SealIdentificationNumberView

import scala.concurrent.Future

class SealIdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val viewModel         = arbitrary[SealIdentificationNumberViewModel].sample.value
  private lazy val formProvider = app.injector.instanceOf[SealIdentificationNumberFormProvider]
  private lazy val form         = formProvider(viewModel.requiredError, Seq.empty)
  private val equipmentMode     = NormalMode
  private val sealMode          = NormalMode

  private val mockViewModelProvider = mock[SealIdentificationNumberViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[SealNavigatorProvider].toInstance(FakeConsignmentNavigators.fakeSealNavigatorProvider),
        bind[SealIdentificationNumberViewModelProvider].toInstance(mockViewModelProvider)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)

    when(mockViewModelProvider.apply(any())(any()))
      .thenReturn(viewModel)
  }

  private lazy val sealIdentificationNumberRoute =
    routes.SealIdentificationNumberController.onPageLoad(arrivalId, equipmentMode, sealMode, equipmentIndex, sealIndex).url

  "SealIdentificationNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, sealIdentificationNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[SealIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, equipmentMode, sealMode, viewModel, equipmentIndex, sealIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "testString")
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, sealIdentificationNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "testString"))

      val view = injector.instanceOf[SealIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, equipmentMode, sealMode, viewModel, equipmentIndex, sealIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, sealIdentificationNumberRoute)
        .withFormUrlEncodedBody(("value", "testString"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, sealIdentificationNumberRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[SealIdentificationNumberView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, equipmentMode, sealMode, viewModel, equipmentIndex, sealIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, sealIdentificationNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, sealIdentificationNumberRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
