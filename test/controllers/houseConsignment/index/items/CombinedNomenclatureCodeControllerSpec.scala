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

package controllers.houseConsignment.index.items

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.CombinedNomenclatureCodeFormProvider
import generators.Generators
import models.NormalMode
import navigation.FakeHouseConsignmentItemNavigatorProviderProvider
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator.HouseConsignmentItemNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.items.CombinedNomenclatureCodePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewModels.houseConsignment.index.items.CombinedNomenclatureCodeViewModel
import viewModels.houseConsignment.index.items.CombinedNomenclatureCodeViewModel.CombinedNomenclatureCodeViewModelProvider
import views.html.houseConsignment.index.items.CombinedNomenclatureCodeView

import scala.concurrent.Future

class CombinedNomenclatureCodeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val viewModel = arbitrary[CombinedNomenclatureCodeViewModel].sample.value

  private val formProvider = new CombinedNomenclatureCodeFormProvider()
  private val form         = formProvider(viewModel.requiredError)

  private val houseConsignmentMode = NormalMode
  private val itemMode             = NormalMode

  lazy val combinedNomenclatureCodeControllerRoute: String =
    routes.CombinedNomenclatureCodeController.onPageLoad(arrivalId, index, index, houseConsignmentMode, itemMode).url

  private val mockViewModelProvider = mock[CombinedNomenclatureCodeViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[HouseConsignmentItemNavigatorProvider].toProvider(classOf[FakeHouseConsignmentItemNavigatorProviderProvider]),
        bind(classOf[CombinedNomenclatureCodeViewModelProvider]).toInstance(mockViewModelProvider)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockViewModelProvider.apply(any(), any(), any(), any(), any())(any()))
      .thenReturn(viewModel)
  }

  "CombinedNomenclatureCodeController" - {

    "must return OK and the correct view for a GET" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, combinedNomenclatureCodeControllerRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[CombinedNomenclatureCodeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, isXI = false, viewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.setValue(CombinedNomenclatureCodePage(index, index), "A1")
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, combinedNomenclatureCodeControllerRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "A1"))

      val view = injector.instanceOf[CombinedNomenclatureCodeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, isXI = false, viewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, combinedNomenclatureCodeControllerRoute)
          .withFormUrlEncodedBody(("value", "A1"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, combinedNomenclatureCodeControllerRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[CombinedNomenclatureCodeView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, isXI = false, viewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, combinedNomenclatureCodeControllerRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, combinedNomenclatureCodeControllerRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

  }
}
