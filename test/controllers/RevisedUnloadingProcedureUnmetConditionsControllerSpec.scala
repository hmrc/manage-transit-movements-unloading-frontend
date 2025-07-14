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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.RevisedUnloadingProcedureUnmetConditionsView

class RevisedUnloadingProcedureUnmetConditionsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private lazy val revisedUnloadingProcedureUnmet: String =
    routes.RevisedUnloadingProcedureUnmetConditionsController.onPageLoad(arrivalId).url

  "RevisedUnloadingProcedureUnmetConditionsController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, revisedUnloadingProcedureUnmet)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[RevisedUnloadingProcedureUnmetConditionsView]

      contentAsString(result) mustEqual
        view(mrn, arrivalId)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, revisedUnloadingProcedureUnmet)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to UnloadingGuidance page on submit" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, revisedUnloadingProcedureUnmet)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.UnloadingGuidanceController.onPageLoad(arrivalId).url
    }
  }
}
