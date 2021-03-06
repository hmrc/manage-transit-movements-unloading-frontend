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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.CannotSendUnloadingRemarksView

class CannotSendUnloadingRemarksControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  "CannotSendUnloadingRemarks Controller" - {

    "must return not found and the correct view for a GET" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, routes.CannotSendUnloadingRemarksController.notFound().url)

      val result = route(app, request).value

      val view = injector.instanceOf[CannotSendUnloadingRemarksView]

      status(result) mustEqual NOT_FOUND

      contentAsString(result) mustEqual view()(request, messages).toString
    }

    "must return bad request and the correct view for a GET" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, routes.CannotSendUnloadingRemarksController.badRequest().url)

      val result = route(app, request).value

      val view = injector.instanceOf[CannotSendUnloadingRemarksView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual view()(request, messages).toString
    }
  }
}
