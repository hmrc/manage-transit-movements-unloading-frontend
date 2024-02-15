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
import models.AuditType.UnloadingRemarks
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.submission.AuditService
import viewModels.CheckYourAnswersViewModel
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockCheckYourAnswersViewModelProvider = mock[CheckYourAnswersViewModelProvider]

  private val mockAuditService = mock[AuditService]

  lazy val checkYourAnswersRoute: String = controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[CheckYourAnswersViewModelProvider].toInstance(mockCheckYourAnswersViewModelProvider),
        bind[AuditService].toInstance(mockAuditService)
      )

  "UnloadingFindingsController Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val sections = arbitrarySections.arbitrary.sample.value

      when(mockCheckYourAnswersViewModelProvider.apply(any())(any()))
        .thenReturn(CheckYourAnswersViewModel(sections))

      val checkYourAnswersViewModel = CheckYourAnswersViewModel(sections)

      val request = FakeRequest(GET, checkYourAnswersRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[CheckYourAnswersView]

      contentAsString(result) mustEqual
        view(mrn, arrivalId, checkYourAnswersViewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" ignore {
      checkArrivalStatus()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, checkYourAnswersRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url

      verify(mockAuditService).audit(eqTo(UnloadingRemarks), eqTo(userAnswers))(any())
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, checkYourAnswersRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" ignore {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, checkYourAnswersRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

  }
}
