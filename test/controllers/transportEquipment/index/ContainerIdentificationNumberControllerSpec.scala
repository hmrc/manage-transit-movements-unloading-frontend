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

package controllers.transportEquipment.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.ContainerIdentificationNumberFormProvider
import generators.Generators
import models.NormalMode
import navigation.TransportEquipmentNavigator
import viewModels.transportEquipment.index.ContainerIdentificationNumberViewModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.ContainerIdentificationNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.transportEquipment.index.ContainerIdentificationNumberViewModel.ContainerIdentificationNumberViewModelProvider
import views.html.transportEquipment.index.ContainerIdentificationNumberView

import scala.concurrent.Future

class ContainerIdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider: ContainerIdentificationNumberFormProvider = new ContainerIdentificationNumberFormProvider()
  private val mockViewModelProvider                                   = mock[ContainerIdentificationNumberViewModelProvider]
  private val viewModel                                               = arbitrary[ContainerIdentificationNumberViewModel].sample.value
  private val mode                                                    = NormalMode
  private val form                                                    = formProvider(viewModel.requiredError, Seq("cin1"))
  private val validAnswer                                             = "1"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[TransportEquipmentNavigator].toInstance(FakeConsignmentNavigators.fakeTransportEquipmentNavigator),
        bind[ContainerIdentificationNumberViewModelProvider].toInstance(mockViewModelProvider)
      )

  when(mockViewModelProvider.apply(any())(any()))
    .thenReturn(viewModel)

  lazy val containerIdentificationNumberRoute: String =
    controllers.transportEquipment.index.routes.ContainerIdentificationNumberController.onPageLoad(arrivalId, equipmentIndex, NormalMode).url

  "ContainerIdentificationNumber Controller" - {

    "must return OK and the correct view for a GET" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, containerIdentificationNumberRoute)
      val result  = route(app, request).value
      val view    = injector.instanceOf[ContainerIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, arrivalId, mrn, equipmentIndex, NormalMode, viewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.setValue(ContainerIdentificationNumberPage(equipmentIndex), validAnswer)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, containerIdentificationNumberRoute)
      val result  = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> validAnswer))
      val view       = injector.instanceOf[ContainerIdentificationNumberView]

      contentAsString(result) mustEqual
        view(filledForm, arrivalId, mrn, equipmentIndex, mode, viewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, containerIdentificationNumberRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val invalidAnswer = ""

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, containerIdentificationNumberRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> invalidAnswer))
      val result    = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[ContainerIdentificationNumberView]

      contentAsString(result) mustEqual
        view(boundForm, arrivalId, mrn, equipmentIndex, mode, viewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, containerIdentificationNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, containerIdentificationNumberRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

  }

}
