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
import controllers.routes
import forms.DescriptionFormProvider
import generators.Generators
import models.{NormalMode, UserAnswers}
import navigation.houseConsignment.index.items.HouseConsignmentItemNavigator.HouseConsignmentItemNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.houseConsignment.index.items.{DeclarationGoodsItemNumberPage, ItemDescriptionPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.GoodsReferenceService
import viewModels.houseConsignment.index.items.DescriptionViewModel
import viewModels.houseConsignment.index.items.DescriptionViewModel.DescriptionViewModelProvider
import views.html.houseConsignment.index.items.DescriptionView

import scala.concurrent.Future
import scala.util.Success

class DescriptionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val mockGoodsReferenceService = mock[GoodsReferenceService]

  private val viewModel = arbitrary[DescriptionViewModel].sample.value

  private lazy val formProvider = new DescriptionFormProvider()
  private lazy val form         = formProvider(viewModel.requiredError)

  private val houseConsignmentMode = NormalMode
  private val itemMode             = NormalMode

  private lazy val itemDescriptionRoute =
    controllers.houseConsignment.index.items.routes.DescriptionController
      .onPageLoad(arrivalId, houseConsignmentMode, itemMode, houseConsignmentIndex, itemIndex)
      .url

  private val mockViewModelProvider = mock[DescriptionViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[HouseConsignmentItemNavigatorProvider]).toInstance(FakeConsignmentItemNavigators.fakeConsignmentItemNavigatorProvider),
        bind(classOf[DescriptionViewModelProvider]).toInstance(mockViewModelProvider),
        bind(classOf[GoodsReferenceService]).toInstance(mockGoodsReferenceService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockViewModelProvider)
    reset(mockGoodsReferenceService)

    when(mockViewModelProvider.apply(any(), any(), any(), any(), any())(any()))
      .thenReturn(viewModel)
  }

  "ItemDescription Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, itemDescriptionRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[DescriptionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, viewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(ItemDescriptionPage(houseConsignmentIndex, itemIndex), "testString")
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, itemDescriptionRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "testString"))

      val view = injector.instanceOf[DescriptionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, viewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      val validAnswer = "testString"

      val userAnswers = emptyUserAnswers

      val userAnswersAfterFirstSet  = userAnswers.setValue(ItemDescriptionPage(houseConsignmentIndex, itemIndex), validAnswer)
      val userAnswersAfterSecondSet = userAnswersAfterFirstSet.setValue(DeclarationGoodsItemNumberPage(houseConsignmentIndex, itemIndex), BigInt(1))

      when(mockGoodsReferenceService.setNextDeclarationGoodsItemNumber(any(), any(), any()))
        .thenReturn(Success(userAnswersAfterSecondSet))

      setExistingUserAnswers(userAnswers)

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, itemDescriptionRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockGoodsReferenceService).setNextDeclarationGoodsItemNumber(eqTo(userAnswersAfterFirstSet), eqTo(houseConsignmentIndex), eqTo(itemIndex))

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue mustEqual userAnswersAfterSecondSet
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, itemDescriptionRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[DescriptionView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, viewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, itemDescriptionRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, itemDescriptionRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
