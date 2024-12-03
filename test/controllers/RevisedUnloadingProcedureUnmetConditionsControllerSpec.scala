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
import generators.Generators
import models.UserAnswers
import navigation.Navigation
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.NewAuthYesNoSubmissionService
import views.html.RevisedUnloadingProcedureUnmetConditionsView

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class RevisedUnloadingProcedureUnmetConditionsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private lazy val revisedUnloadingProcedureUnmet: String = controllers.routes.RevisedUnloadingProcedureUnmetConditionsController.onPageLoad(arrivalId).url
  private val mockService                                 = mock[NewAuthYesNoSubmissionService]
  implicit private val ec: ExecutionContext               = ExecutionContext.global

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[Navigation].toInstance(fakeNavigation),
        bind[NewAuthYesNoSubmissionService].toInstance(mockService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockService)
  }

  "RevisedUnloadingProcedureUnmetConditionsController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, revisedUnloadingProcedureUnmet)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[RevisedUnloadingProcedureUnmetConditionsView]

      contentAsString(result) mustEqual
        view(mrn, arrivalId)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, revisedUnloadingProcedureUnmet)

      val result = route(app, request).value

      status(result) `mustEqual` SEE_OTHER

      redirectLocation(result).value `mustEqual` routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to UnloadingGuidance page and set newAuthYesNo to No(false) on submit" in {
      val now         = Instant.now()
      val userAnswers = emptyUserAnswers.copy(lastUpdated = now)
      val updatedUserAnswer = emptyUserAnswers.copy(
        data = Json
          .parse(s"""
               |{
               |  "otherQuestions" : {
               |    "newAuthYesNo" : false
               |  }
               |}
               |""".stripMargin)
          .as[JsObject],
        lastUpdated = now
      )
      setExistingUserAnswers(userAnswers)
      val request = FakeRequest(POST, revisedUnloadingProcedureUnmet)
      when(mockService.updateUserAnswers(any(), any())(any(), any())).thenReturn(Future.successful(updatedUserAnswer))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result = route(app, request).value

      status(result) `mustEqual` SEE_OTHER
      redirectLocation(result).value `mustEqual` controllers.routes.UnloadingGuidanceController.onPageLoad(arrivalId).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue mustBe updatedUserAnswer
    }
  }
}
