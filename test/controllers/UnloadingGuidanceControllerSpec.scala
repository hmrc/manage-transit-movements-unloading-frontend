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
import matchers.JsonMatchers
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{GoodsTooLargeForContainerYesNoPage, NewAuthYesNoPage, RevisedUnloadingProcedureConditionsYesNoPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewModels.UnloadingGuidanceViewModel.UnloadingGuidanceViewModelProvider
import viewModels.{Para3, UnloadingGuidanceViewModel}
import views.html.UnloadingGuidanceView

import scala.concurrent.Future

class UnloadingGuidanceControllerSpec extends SpecBase with Generators with AppWithDefaultMockFixtures with JsonMatchers {

  lazy val unloadingGuidanceRoute: String               = routes.UnloadingGuidanceController.onPageLoad(arrivalId).url
  val mockViewModel: UnloadingGuidanceViewModelProvider = mock[UnloadingGuidanceViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingGuidanceViewModelProvider].toInstance(mockViewModel))

  "UnloadingGuidance Controller" - {
    "return OK and the correct view for a GET when message is Unloading Permission(IE043)" in {

      when(mockUnloadingPermissionMessageService.getMessageId(any(), any())(any(), any())).thenReturn(Future.successful(Some(messageId)))

      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)
      )

      when(mockViewModel.apply(newAuth = false, goodsTooLarge = Some(true)))
        .thenReturn(
          UnloadingGuidanceViewModel(
            title = "title",
            heading = "heading",
            preLinkText = Some("preLinkText"),
            linkText = "link",
            postLinkText = Some("postLinkText"),
            para1 = Some("para1"),
            para2 = Some("para2"),
            para3 = Some(Para3.apply("unloadingGuidance"))
          )
        )

      val request = FakeRequest(GET, unloadingGuidanceRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[UnloadingGuidanceView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(mrn, arrivalId, messageId, NormalMode, mockViewModel.apply(newAuth = false, goodsTooLarge = Some(true)))(
        request,
        messages
      ).toString
    }

    "must redirect to LargeUnsealedGoodsRecordDiscrepanciesYesNoController" - {
      "when NewAuthYesNoPage is true and GoodsTooLargeForContainerYesNoPage is true" in {

        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, unloadingGuidanceRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.LargeUnsealedGoodsRecordDiscrepanciesYesNoController.onPageLoad(userAnswers.id, NormalMode).url
      }
    }

    "must redirect to SealsReplacedByCustomsAuthorityYesNoController" - {
      "when NewAuthYesNoPage is true and GoodsTooLargeForContainerYesNoPage is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)
          .setValue(GoodsTooLargeForContainerYesNoPage, false)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, unloadingGuidanceRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SealsReplacedByCustomsAuthorityYesNoController.onPageLoad(userAnswers.id, NormalMode).url
      }
    }

    "must redirect to GoodsTooLargeForContainerYesNoController" - {
      "when NewAuthYesNoPage is true and GoodsTooLargeForContainerYesNoPage is undefined" in {

        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, unloadingGuidanceRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.GoodsTooLargeForContainerYesNoController.onPageLoad(userAnswers.id, NormalMode).url
      }
    }

    "must redirect to UnloadingTypeController" - {
      "when NewAuthYesNoPage is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, unloadingGuidanceRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.UnloadingTypeController.onPageLoad(userAnswers.id, NormalMode).url
      }
    }

    "must redirect to UnloadingTypeController" - {
      "when RevisedUnloadingProcedureConditions is false" in {

        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)
          .setValue(RevisedUnloadingProcedureConditionsYesNoPage, false)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, unloadingGuidanceRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.UnloadingTypeController.onPageLoad(userAnswers.id, NormalMode).url
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, unloadingGuidanceRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a GET if no answer is found for NewAuthYesNoPage" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, unloadingGuidanceRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, unloadingGuidanceRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no answer is found for NewAuthYesNoPage" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, unloadingGuidanceRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
