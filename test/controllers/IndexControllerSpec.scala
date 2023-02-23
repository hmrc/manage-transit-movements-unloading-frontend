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
import extractors.UnloadingPermissionExtractor
import generators.Generators
import models.{UnloadingPermission, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnloadingPermissionService

import scala.concurrent.Future
import scala.util.{Failure, Success}

class IndexControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  val sampleUnloadingPermission: UnloadingPermission = arbitrary[UnloadingPermission].sample.value

  private val mockUnloadingPermissionService = mock[UnloadingPermissionService]
  private val mockExtractor                  = mock[UnloadingPermissionExtractor]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingPermissionService); reset(mockExtractor)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingPermissionService].toInstance(mockUnloadingPermissionService))
      .overrides(bind[UnloadingPermissionExtractor].toInstance(mockExtractor))

  private lazy val nextPage = controllers.P5.routes.UnloadingGuidanceController.onPageLoad(arrivalId).url

  "Index Controller" - {

    "unloadingRemarks" - {
      "must redirect to onward route for a GET when there are no UserAnswers and prepopulated data" in {
        checkArrivalStatus()
        val unloadingPermission = sampleUnloadingPermission.copy(movementReferenceNumber = mrn.toString)

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
          .thenReturn(Future.successful(Some(unloadingPermission)))

        val userAnswers = emptyUserAnswers

        when(mockExtractor.apply(any(), any())(any(), any())).thenReturn(Future.successful(Success(userAnswers)))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        setNoExistingUserAnswers()

        val request                = FakeRequest(GET, routes.IndexController.unloadingRemarks(arrivalId).url)
        val result: Future[Result] = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual nextPage

        verify(mockSessionRepository).set(userAnswersCaptor.capture())

        userAnswersCaptor.getValue mustBe userAnswers
      }

      "must redirect to onward route when there are UserAnswers" in {
        checkArrivalStatus()
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, routes.IndexController.unloadingRemarks(arrivalId).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual nextPage
      }

      "must redirect to session expired when no response for arrivalId" in {
        checkArrivalStatus()
        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
          .thenReturn(Future.successful(None))

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, routes.IndexController.unloadingRemarks(arrivalId).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }

      "must redirect to session expired when bad mrn received" in {
        checkArrivalStatus()
        val badUnloadingPermission = sampleUnloadingPermission.copy(movementReferenceNumber = "")

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
          .thenReturn(Future.successful(Some(badUnloadingPermission)))

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, routes.IndexController.unloadingRemarks(arrivalId).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }

      "must redirect to session expired when extractor fails" in {
        checkArrivalStatus()

        val unloadingPermission = sampleUnloadingPermission.copy(movementReferenceNumber = mrn.toString)

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
          .thenReturn(Future.successful(Some(unloadingPermission)))

        when(mockExtractor.apply(any(), any())(any(), any())).thenReturn(Future.successful(Failure(new Throwable(""))))

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, routes.IndexController.unloadingRemarks(arrivalId).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }
    }

    "newUnloadingRemarks" - {
      "must redirect to session expired if no user answers" in {
        checkArrivalStatus()
        setNoExistingUserAnswers()

        val request                = FakeRequest(GET, routes.IndexController.newUnloadingRemarks(arrivalId).url)
        val result: Future[Result] = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }

      "must extract unloading permission to user answers" in {
        checkArrivalStatus()
        val unloadingPermission = sampleUnloadingPermission.copy(movementReferenceNumber = mrn.toString)

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
          .thenReturn(Future.successful(Some(unloadingPermission)))

        val userAnswers = emptyUserAnswers.copy(data = Json.obj("foo" -> "bar"))

        when(mockExtractor.apply(any(), any())(any(), any())).thenReturn(Future.successful(Success(userAnswers)))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        setExistingUserAnswers(emptyUserAnswers)

        val request                = FakeRequest(GET, routes.IndexController.newUnloadingRemarks(arrivalId).url)
        val result: Future[Result] = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual nextPage

        verify(mockSessionRepository).set(userAnswersCaptor.capture())

        userAnswersCaptor.getValue mustBe userAnswers
      }
    }
  }
}
