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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigation
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import pages.NewAuthYesNoPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.UserAnswersService
import views.html.NewAuthYesNoView

import scala.concurrent.Future

class NewAuthYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider           = new YesNoFormProvider()
  private val form                   = formProvider("newAuthYesNo")
  private val mode                   = NormalMode
  private val mockUserAnswersService = mock[UserAnswersService]

  private lazy val newAuthYesNoRoute =
    controllers.routes.NewAuthYesNoController.onPageLoad(arrivalId, mode).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[Navigation].toInstance(fakeNavigation),
        bind[UserAnswersService].toInstance(mockUserAnswersService)
      )

  "NewAuthYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, newAuthYesNoRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[NewAuthYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(NewAuthYesNoPage, true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, newAuthYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[NewAuthYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, mode)(request, messages).toString
    }

    "must redirect to the next page when yes is submitted and answer has changed" in {
      val jsonBeforeEverything = Json
        .parse(s"""
             |{
             |  "otherQuestions" : {
             |    "foo" : "bar",
             |    "otherThingsToReport" : "other things"
             |  },
             |  "someDummyTransformedData" : {
             |    "foo" : "bar"
             |  },
             |  "someDummyDiscrepancies" : {
             |    "foo" : "bar"
             |  }
             |}
             |""".stripMargin)
        .as[JsObject]

      val jsonAfterTransformation = Json
        .parse(s"""
             |{
             |  "someDummyTransformedData" : {
             |    "foo" : "bar"
             |  },
             |  "otherQuestions" : {
             |    "newAuthYesNo" : true
             |  }
             |}
             |""".stripMargin)
        .as[JsObject]

      val userAnswers                    = emptyUserAnswers
      val userAnswersBeforeEverything    = userAnswers.copy(data = jsonBeforeEverything)
      val userAnswersAfterTransformation = userAnswers.copy(data = jsonAfterTransformation)

      setExistingUserAnswers(userAnswersBeforeEverything)

      when(mockUserAnswersService.retainAndTransform(any(), any())(any(), any()))
        .thenReturn(Future.successful(userAnswersAfterTransformation))

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, newAuthYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue mustBe userAnswersAfterTransformation
    }

    "must redirect to the next page when yes is submitted and answer has not changed" in {

      val userAnswers = emptyUserAnswers.setValue(NewAuthYesNoPage, true)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, newAuthYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      verifyNoInteractions(mockUserAnswersService)
    }

    "must redirect to the next page when no is submitted and answer has not changed" in {

      val userAnswers = emptyUserAnswers.setValue(NewAuthYesNoPage, false)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, newAuthYesNoRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      verifyNoInteractions(mockUserAnswersService)
    }

    "must redirect to the next page when no is submitted" in {

      val userAnswers = emptyUserAnswers

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, newAuthYesNoRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      verifyNoInteractions(mockUserAnswersService)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, newAuthYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[NewAuthYesNoView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, newAuthYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, newAuthYesNoRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
