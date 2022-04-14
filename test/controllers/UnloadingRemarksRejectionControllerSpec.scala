/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.LocalDate

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.MessagesModelGenerators
import models.{FunctionalError, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnloadingRemarksRejectionService
import viewModels.sections.Section
import views.html.{UnloadingRemarksMultipleErrorsRejectionView, UnloadingRemarksRejectionView}

import scala.concurrent.Future

class UnloadingRemarksRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MessagesModelGenerators {

  private val mockUnloadingRemarksRejectionService = mock[UnloadingRemarksRejectionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingRemarksRejectionService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockUnloadingRemarksRejectionService))

  "UnloadingRemarksRejection Controller" - {

    "return OK and the single error rejection view for a GET when unloading rejection message returns a Some" in {

      val functionalError = arbitrary[FunctionalError](arbitraryRejectionErrorNonDefaultPointer).sample.value

      val errors: Seq[FunctionalError] = Seq(functionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = injector.instanceOf[UnloadingRemarksRejectionView]

      val expectedSection: Seq[Section] = Nil

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(arrivalId, expectedSection)(request, messages).toString
    }

    "return OK and the multiple error rejection view for a GET when unloading rejection message returns Some" in {
      val functionalError = arbitrary[FunctionalError](arbitraryRejectionError).sample.value

      val errors = Seq(functionalError, functionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value
      val view   = injector.instanceOf[UnloadingRemarksMultipleErrorsRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(arrivalId, errors)(request, messages).toString
    }

    "render 'Technical difficulties' page when unloading rejection message's has no errors" in {

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, Seq.empty))))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }

    "render the 'Technical difficulties' page when unloading rejection message returns a None" in {
      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(None))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      verify(mockUnloadingRemarksRejectionService, times(1)).unloadingRemarksRejectionMessage(any())(any())
      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }
  }
}
