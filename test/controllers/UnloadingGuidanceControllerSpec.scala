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
import matchers.JsonMatchers
import models.NormalMode
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.UnloadingGuidanceView

class UnloadingGuidanceControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers {

  "UnloadingGuidance Controller" - {
    "return OK and the correct view for a GET" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.UnloadingGuidanceController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[UnloadingGuidanceView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(mrn, arrivalId)(request, messages).toString
    }

    "must redirect to 'date goods unloaded' for a POST" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, routes.UnloadingGuidanceController.onSubmit(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.DateGoodsUnloadedController.onPageLoad(arrivalId, NormalMode).url
    }
  }
}
