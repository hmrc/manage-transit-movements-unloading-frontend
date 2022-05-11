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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.VehicleNameRegistrationReferencePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnloadingRemarksService
import viewModels.RejectionCheckYourAnswersViewModel
import viewModels.sections.Section
import views.html.RejectionCheckYourAnswersView

import scala.concurrent.Future

class RejectionCheckYourAnswersControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val mockUnloadingRemarksService                       = mock[UnloadingRemarksService]
  private val mockViewModel: RejectionCheckYourAnswersViewModel = mock[RejectionCheckYourAnswersViewModel]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingRemarksService, mockViewModel)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService))
      .overrides(bind[RejectionCheckYourAnswersViewModel].toInstance(mockViewModel))

  "return OK and the Rejection view for a GET when unloading rejection message returns a Some" in {
    checkArrivalStatus()

    val sampleSections = listWithMaxLength[Section]().sample.value

    when(mockViewModel.apply(any())(any())).thenReturn(sampleSections)

    setExistingUserAnswers(emptyUserAnswers)

    val request = FakeRequest(GET, routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url)

    val result: Future[Result] = route(app, request).value

    val view = injector.instanceOf[RejectionCheckYourAnswersView]

    status(result) mustEqual OK

    contentAsString(result) mustEqual
      view(mrn, arrivalId, sampleSections)(request, messages).toString
  }

  "onSubmit" - {
    "must redirect to Confirmation on valid submission" in {
      checkArrivalStatus()
      val userAnswers = emptyUserAnswers.setValue(VehicleNameRegistrationReferencePage, "updatedValue")

      when(mockUnloadingRemarksService.resubmit(any(), any())(any()))
        .thenReturn(Future.successful(Some(ACCEPTED)))

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, routes.RejectionCheckYourAnswersController.onSubmit(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ConfirmationController.onPageLoad(arrivalId).url
    }

    "return BadRequest when backend returns 401" in {
      checkArrivalStatus()
      val userAnswers = emptyUserAnswers.setValue(VehicleNameRegistrationReferencePage, "updatedValue")

      setExistingUserAnswers(userAnswers)

      when(mockUnloadingRemarksService.resubmit(any(), any())(any())).thenReturn(Future.successful(Some(UNAUTHORIZED)))

      val request = FakeRequest(POST, routes.RejectionCheckYourAnswersController.onSubmit(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.badRequest().url
    }

    "return Technical Difficulties on internal failure" in {
      checkArrivalStatus()
      val userAnswers = emptyUserAnswers.setValue(VehicleNameRegistrationReferencePage, "updatedValue")

      setExistingUserAnswers(userAnswers)

      when(mockUnloadingRemarksService.resubmit(any(), any())(any())).thenReturn(Future.successful(None))

      val request = FakeRequest(POST, routes.RejectionCheckYourAnswersController.onSubmit(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }
  }
}
