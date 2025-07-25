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

package controllers.houseConsignment.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import models.{Index, NormalMode, UserAnswers}
import navigation.FakeHouseConsignmentNavigatorProvider
import navigation.houseConsignment.index.HouseConsignmentNavigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import pages.houseConsignment.index.AddItemYesNoPage
import pages.houseConsignment.index.items.DeclarationGoodsItemNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.GoodsReferenceService
import views.html.houseConsignment.index.AddItemYesNoView

import scala.concurrent.Future
import scala.util.Success

class AddItemYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("houseConsignment.addItemYesNo", houseConsignmentIndex.display)

  private val houseConsignmentMode = NormalMode

  private lazy val addItemYesNoRoute =
    routes.AddItemYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, houseConsignmentMode).url

  private val mockGoodsReferenceService = mock[GoodsReferenceService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[HouseConsignmentNavigator].toProvider(classOf[FakeHouseConsignmentNavigatorProvider]),
        bind(classOf[GoodsReferenceService]).toInstance(mockGoodsReferenceService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockGoodsReferenceService)
  }

  "AddItemYesNoController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, addItemYesNoRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[AddItemYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, houseConsignmentIndex, houseConsignmentMode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(AddItemYesNoPage(houseConsignmentIndex), true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, addItemYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddItemYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentIndex, houseConsignmentMode)(request, messages).toString
    }

    "must redirect to the next page when valid yes is submitted" in {
      val nextIndex      = Index(0)
      val initialAnswers = emptyUserAnswers
      val answersAfterSet = initialAnswers
        .setValue(AddItemYesNoPage(houseConsignmentIndex), true)
        .setValue(DeclarationGoodsItemNumberPage(houseConsignmentIndex, nextIndex), BigInt(1))

      setExistingUserAnswers(initialAnswers)

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      when(mockGoodsReferenceService.setNextDeclarationGoodsItemNumber(any(), any(), any()))
        .thenReturn(Success(answersAfterSet))

      val request = FakeRequest(POST, addItemYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue mustEqual answersAfterSet
    }

    "must redirect to the next page when valid no is submitted" in {
      val initialAnswers = emptyUserAnswers
      val answersAfterSet = initialAnswers
        .setValue(AddItemYesNoPage(houseConsignmentIndex), false)

      setExistingUserAnswers(initialAnswers)

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, addItemYesNoRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue mustEqual answersAfterSet

      verifyNoInteractions(mockGoodsReferenceService)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, addItemYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[AddItemYesNoView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, houseConsignmentIndex, houseConsignmentMode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addItemYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addItemYesNoRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
