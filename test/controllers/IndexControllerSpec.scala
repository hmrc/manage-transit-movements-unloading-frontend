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
import models.{UnloadingPermission, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnloadingPermissionService

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  val sampleUnloadingPermission: UnloadingPermission = arbitrary[UnloadingPermission].sample.value

  private val mockUnloadingPermissionService = mock[UnloadingPermissionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingPermissionService);
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingPermissionService].toInstance(mockUnloadingPermissionService))

  private lazy val nextPage = controllers.routes.UnloadingGuidanceController.onPageLoad(arrivalId).url

  "Index Controller" ignore {

    "unloadingRemarks" - {
      "must redirect to onward route for a GET when there are no UserAnswers and prepopulated data" in {
        checkArrivalStatus()
        val unloadingPermission = sampleUnloadingPermission.copy(movementReferenceNumber = mrn.toString)

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any()))
          .thenReturn(Future.successful(Some(unloadingPermission)))

        val userAnswers = emptyUserAnswers

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

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, routes.IndexController.unloadingRemarks(arrivalId).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }
    }
  }
}
