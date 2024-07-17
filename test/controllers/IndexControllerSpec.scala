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
import controllers.actions.FakeUnloadingPermissionAction
import generators.Generators
import models.{ArrivalId, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DateTimeService
import utils.transformers.IE043Transformer

import java.time.LocalDateTime
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private lazy val nextPage = controllers.routes.UnloadingGuidanceController.onPageLoad(arrivalId).url

  private val mockDateTimeService: DateTimeService = mock[DateTimeService]

  private lazy val mockIE043Transformer = mock[IE043Transformer]

  private val dateTime = LocalDateTime.of(2023: Int, 1, 1, 0, 0)

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DateTimeService].toInstance(mockDateTimeService),
        bind[IE043Transformer].toInstance(mockIE043Transformer)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockDateTimeService)
    reset(mockIE043Transformer)

    when(mockDateTimeService.currentDateTime).thenReturn(dateTime)

    when(mockIE043Transformer.transform(any())(any(), any()))
      .thenReturn(Future.successful(emptyUserAnswers.copy(data = Json.obj("foo" -> "bar"))))
  }

  "Index Controller" - {

    "unloadingRemarks" - {
      "must redirect to onward route for a GET when there are no UserAnswers and prepopulated data" in {
        val request = FakeRequest(GET, routes.IndexController.unloadingRemarks(arrivalId).url)

        val unloadingAction: FakeUnloadingPermissionAction = new FakeUnloadingPermissionAction(
          ArrivalId("AB123"),
          mockUnloadingPermissionMessageService
        )

        when(mockSessionRepository.get(any(), any())) thenReturn Future.successful(None)
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        when(mockUnloadingPermissionActionProvider.apply(any())) thenReturn unloadingAction

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual nextPage

        verify(mockIE043Transformer).transform(userAnswersCaptor.capture())(any(), any())
        userAnswersCaptor.getValue.data mustBe Json.obj()

        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.data mustBe Json.obj("foo" -> "bar")
        userAnswersCaptor.getValue.ie043Data mustBe basicIe043
      }

      "must redirect to onward route when there are UserAnswers" in {

        val request = FakeRequest(GET, routes.IndexController.unloadingRemarks(arrivalId).url)

        val unloadingAction: FakeUnloadingPermissionAction = new FakeUnloadingPermissionAction(
          ArrivalId("AB123"),
          mockUnloadingPermissionMessageService
        )

        when(mockSessionRepository.get(any(), any())) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        when(mockUnloadingPermissionActionProvider.apply(any())) thenReturn unloadingAction

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual nextPage

        verifyNoInteractions(mockIE043Transformer)

        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.data mustBe emptyUserAnswers.data
        userAnswersCaptor.getValue.ie043Data mustBe emptyUserAnswers.ie043Data
      }
    }
  }
}
