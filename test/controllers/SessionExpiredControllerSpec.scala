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
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SessionExpiredControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  "Session Expired Controller" - {

    "must return OK and the correct view for a GET" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, routes.SessionExpiredController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual OK
    }

    "must redirect to a new page for a POST" in {
      val request =
        FakeRequest(POST, routes.SessionExpiredController.onSubmit().url)
          .withFormUrlEncodedBody()

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual "http://localhost:9485/manage-transit-movements/what-do-you-want-to-do"
    }
  }
}
