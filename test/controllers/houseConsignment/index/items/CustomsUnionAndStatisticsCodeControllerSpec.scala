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
import forms.CUSCodeFormProvider
import generators.Generators
import models.NormalMode
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator.HouseConsignmentItemNavigatorProvider
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.items.CustomsUnionAndStatisticsCodePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ReferenceDataService
import viewModels.houseConsignment.index.items.CustomsUnionAndStatisticsCodeViewModel
import viewModels.houseConsignment.index.items.CustomsUnionAndStatisticsCodeViewModel.CustomsUnionAndStatisticsCodeViewModelProvider
import views.html.houseConsignment.index.items.CustomsUnionAndStatisticsCodeView

import scala.concurrent.Future

class CustomsUnionAndStatisticsCodeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val viewModel = arbitrary[CustomsUnionAndStatisticsCodeViewModel].sample.value

  private val formProvider = new CUSCodeFormProvider()
  private val form         = formProvider("houseConsignment.item.customsUnionAndStatisticsCode", viewModel.requiredError)

  private val houseConsignmentMode = NormalMode
  private val itemMode             = NormalMode

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private lazy val customsUnionAndStatisticsCodeRoute =
    routes.CustomsUnionAndStatisticsCodeController
      .onPageLoad(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
      .url

  private val mockViewModelProvider = mock[CustomsUnionAndStatisticsCodeViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[HouseConsignmentItemNavigatorProvider]).toInstance(FakeConsignmentItemNavigators.fakeConsignmentItemNavigatorProvider),
        bind[ReferenceDataService].toInstance(mockReferenceDataService),
        bind(classOf[CustomsUnionAndStatisticsCodeViewModelProvider]).toInstance(mockViewModelProvider)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockViewModelProvider.apply(any(), any(), any(), any(), any())(any()))
      .thenReturn(viewModel)
  }

  "CustomsUnionAndStatisticsCode Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, customsUnionAndStatisticsCodeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[CustomsUnionAndStatisticsCodeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, viewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex), "validCode")
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, customsUnionAndStatisticsCodeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "validCode"))

      val view = injector.instanceOf[CustomsUnionAndStatisticsCodeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, viewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockReferenceDataService.doesCUSCodeExist(anyString())(any(), any())).thenReturn(Future.successful(true))

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, customsUnionAndStatisticsCodeRoute)
        .withFormUrlEncodedBody(("value", "0010007-2"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, customsUnionAndStatisticsCodeRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[CustomsUnionAndStatisticsCodeView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, viewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, customsUnionAndStatisticsCodeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, customsUnionAndStatisticsCodeRoute)
        .withFormUrlEncodedBody(("value", "validCode"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
