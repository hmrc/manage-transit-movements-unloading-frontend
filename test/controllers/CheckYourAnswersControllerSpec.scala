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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.submission.{AuditService, SubmissionService}
import uk.gov.hmrc.http.HttpResponse
import viewModels.CheckYourAnswersViewModel
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private lazy val mockCheckYourAnswersViewModelProvider = mock[CheckYourAnswersViewModelProvider]
  private lazy val mockSubmissionService                 = mock[SubmissionService]
  private lazy val mockAuditService                      = mock[AuditService]

  lazy val checkYourAnswersRoute: String = controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[CheckYourAnswersViewModelProvider].toInstance(mockCheckYourAnswersViewModelProvider),
        bind[SubmissionService].toInstance(mockSubmissionService),
        bind[AuditService].toInstance(mockAuditService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCheckYourAnswersViewModelProvider)
    reset(mockSubmissionService)
    reset(mockAuditService)
  }

  "UnloadingFindingsController Controller" - {

    "must return OK and the correct view for a GET" in {
      setExistingUserAnswers(emptyUserAnswers)

      val checkYourAnswersViewModel = arbitrary[CheckYourAnswersViewModel].sample.value

      when(mockCheckYourAnswersViewModelProvider.apply(any())(any()))
        .thenReturn(checkYourAnswersViewModel)

      val request = FakeRequest(GET, checkYourAnswersRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[CheckYourAnswersView]

      contentAsString(result) mustEqual
        view(mrn, arrivalId, checkYourAnswersViewModel)(request, messages).toString
    }

    "must redirect to the next page when submission successful" in {
      when(mockSubmissionService.submit(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val userAnswers = emptyUserAnswers
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, checkYourAnswersRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url

      verify(mockAuditService).audit(eqTo(UnloadingRemarks), eqTo(userAnswers))(any())
    }

    "must redirect to tech difficulties when submission unsuccessful" in {
      forAll(Gen.choose(400: Int, 599: Int)) {
        errorCode =>
          when(mockSubmissionService.submit(any(), any())(any()))
            .thenReturn(Future.successful(HttpResponse(errorCode, "")))

          val userAnswers = emptyUserAnswers
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, checkYourAnswersRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url

          verifyNoInteractions(mockAuditService)
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, checkYourAnswersRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, checkYourAnswersRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
