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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.RevisedUnloadingProcedureConditionsYesNoPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.UsersAnswersService
import views.html.RevisedUnloadingProcedureConditionsYesNoView

import java.time.Instant
import scala.concurrent.Future

class RevisedUnloadingProcedureConditionsYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private lazy val revisedUnloadingProcedureConditionsYesNoRoute =
    controllers.routes.RevisedUnloadingProcedureConditionsYesNoController.onPageLoad(arrivalId, mode).url
  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("revisedUnloadingProcedureConditionsYesNo")
  private val mode         = NormalMode
  private val mockService  = mock[UsersAnswersService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[Navigation].toInstance(fakeNavigation),
        bind[UsersAnswersService].toInstance(mockService)
      )

  "RevisedUnloadingProcedureConditionsYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, revisedUnloadingProcedureConditionsYesNoRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[RevisedUnloadingProcedureConditionsYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, revisedUnloadingProcedureConditionsYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[RevisedUnloadingProcedureConditionsYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, mode)(request, messages).toString
    }

    "must redirect to the next page after transformation when valid data is submitted" in {

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
             |  }
             |}
             |""".stripMargin)
        .as[JsObject]

      val now                            = Instant.now()
      val userAnswersBeforeEverything    = emptyUserAnswers.copy(data = jsonBeforeEverything, lastUpdated = now)
      val userAnswersAfterTransformation = emptyUserAnswers.copy(data = jsonAfterTransformation, lastUpdated = now)

      setExistingUserAnswers(userAnswersBeforeEverything)

      when(mockService.updateConditionalAndWipe(any(), any(), any())(any(), any())).thenReturn(Future.successful(userAnswersAfterTransformation))

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, revisedUnloadingProcedureConditionsYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must redirect to the next page when user answer is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)
      when(mockService.updateConditionalAndWipe(any(), any(), any())(any(), any())).thenReturn(Future.successful(emptyUserAnswers))

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, revisedUnloadingProcedureConditionsYesNoRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, revisedUnloadingProcedureConditionsYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RevisedUnloadingProcedureConditionsYesNoView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, revisedUnloadingProcedureConditionsYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, revisedUnloadingProcedureConditionsYesNoRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
