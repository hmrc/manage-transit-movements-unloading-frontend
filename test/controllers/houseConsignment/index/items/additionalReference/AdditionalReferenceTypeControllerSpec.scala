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

package controllers.houseConsignment.index.items.additionalReference

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.SelectableFormProvider
import generators.Generators
import models.{NormalMode, SelectableList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.items.additionalReference.AdditionalReferenceTypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AdditionalReferencesService
import viewModels.houseConsignment.index.items.additionalReference.AdditionalReferenceTypeViewModel
import viewModels.houseConsignment.index.items.additionalReference.AdditionalReferenceTypeViewModel.AdditionalReferenceTypeViewModelProvider
import views.html.houseConsignment.index.items.additionalReference.AdditionalReferenceTypeView

import scala.concurrent.Future

class AdditionalReferenceTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val additionalReference1    = arbitraryAdditionalReference.arbitrary.sample.get
  private val additionalReference2    = arbitraryAdditionalReference.arbitrary.sample.get
  private val additionalReferenceList = SelectableList(Seq(additionalReference1, additionalReference2))

  private val mockViewModelProvider = mock[AdditionalReferenceTypeViewModelProvider]
  private val viewModel             = arbitrary[AdditionalReferenceTypeViewModel].sample.value

  private val mockAdditionalReferencesService: AdditionalReferencesService = mock[AdditionalReferencesService]
  private val formProvider                                                 = new SelectableFormProvider()
  private val mode                                                         = NormalMode
  private val form                                                         = formProvider(mode, "houseConsignment.index.items.additionalReference.additionalReferenceType", additionalReferenceList)

  private lazy val additionalReferenceRoute =
    routes.AdditionalReferenceTypeController.onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AdditionalReferenceTypeViewModelProvider]).toInstance(mockViewModelProvider))
      .overrides(bind(classOf[AdditionalReferencesService]).toInstance(mockAdditionalReferencesService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)

    when(mockViewModelProvider.apply(any(), any(), any(), any(), any())(any()))
      .thenReturn(viewModel)
  }

  "AdditionalReference Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockAdditionalReferencesService.getAdditionalReferences()(any())).thenReturn(Future.successful(additionalReferenceList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, additionalReferenceRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[AdditionalReferenceTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, additionalReferenceList.values, viewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockAdditionalReferencesService.getAdditionalReferences()(any())).thenReturn(Future.successful(additionalReferenceList))
      val userAnswers = emptyUserAnswers.setValue(AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex), additionalReference1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, additionalReferenceRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> additionalReference1.value))

      val view = injector.instanceOf[AdditionalReferenceTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, additionalReferenceList.values, viewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockAdditionalReferencesService.getAdditionalReferences()(any())).thenReturn(Future.successful(additionalReferenceList))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, additionalReferenceRoute)
        .withFormUrlEncodedBody(("value", additionalReference1.value))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.houseConsignment.index.items.additionalReference.routes.AddAdditionalReferenceNumberYesNoController
        .onPageLoad(arrivalId, mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
        .url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockAdditionalReferencesService.getAdditionalReferences()(any())).thenReturn(Future.successful(additionalReferenceList))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, additionalReferenceRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[AdditionalReferenceTypeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, mrn, additionalReferenceList.values, viewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, additionalReferenceRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, additionalReferenceRoute)
        .withFormUrlEncodedBody(("value", additionalReference1.value))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
