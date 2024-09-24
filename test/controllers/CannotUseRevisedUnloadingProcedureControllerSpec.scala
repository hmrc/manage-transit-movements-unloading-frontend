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
import models.NormalMode
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.CannotUseRevisedUnloadingProcedureView

class CannotUseRevisedUnloadingProcedureControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  private val mode = NormalMode

  lazy val cannotUseRevisedUnloadingProcedureRoute: String = controllers.routes.CannotUseRevisedUnloadingProcedureController.onPageLoad(arrivalId).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "CannotUseRevisedUnloadingProcedureController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, cannotUseRevisedUnloadingProcedureRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[CannotUseRevisedUnloadingProcedureView]

      contentAsString(result) mustEqual
        view(mrn, arrivalId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, cannotUseRevisedUnloadingProcedureRoute)

      val result = route(app, request).value

      status(result) `mustEqual` SEE_OTHER

      redirectLocation(result).value `mustEqual` routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to NewAuthYesNo page" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, cannotUseRevisedUnloadingProcedureRoute)

      val result = route(app, request).value

      status(result) `mustEqual` SEE_OTHER
      redirectLocation(result).value `mustEqual` controllers.routes.NewAuthYesNoController.onPageLoad(arrivalId, NormalMode).url
    }

  }
}
