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
import pages.{GoodsTooLargeForContainerYesNoPage, NewAuthYesNoPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.UnloadingGuidanceViewModel
import viewModels.UnloadingGuidanceViewModel.UnloadingGuidanceViewModelProvider
import views.html.UnloadingGuidanceView

class UnloadingGuidanceControllerSpec extends SpecBase with Generators with AppWithDefaultMockFixtures with JsonMatchers {

  private val unloadingGuidanceRoute                = routes.UnloadingGuidanceController.onPageLoad(arrivalId, messageId).url
  val viewModel: UnloadingGuidanceViewModelProvider = mock[UnloadingGuidanceViewModelProvider]

  "UnloadingGuidance Controller" - {
    "return OK and the correct view for a GET when message is Unloading Permission(IE043)" in {
      checkArrivalStatus()

      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)
      )

      val request = FakeRequest(GET, unloadingGuidanceRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[UnloadingGuidanceView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(mrn, arrivalId, newAuth = false, goodsTooLarge = true, messageId, NormalMode, viewModel.apply())(
        request,
        messages
      ).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()

      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)
      )
      val request = FakeRequest(GET, unloadingGuidanceRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to unloading type for a POST" in {
      checkArrivalStatus()

      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)
      )
      val request = FakeRequest(POST, unloadingGuidanceRoute)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result).value mustBe routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode).url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      checkArrivalStatus()

      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(NewAuthYesNoPage, false)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)
      )
      val request = FakeRequest(POST, unloadingGuidanceRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
