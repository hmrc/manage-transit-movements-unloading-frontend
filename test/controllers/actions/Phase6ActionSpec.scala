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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.requests.DataRequest
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.*
import play.api.mvc.Results.*
import play.api.test.Helpers.*

import scala.concurrent.Future

class Phase6ActionSpec extends SpecBase with BeforeAndAfterEach with Generators with AppWithDefaultMockFixtures {

  private def fakeOkResult[A]: A => Future[Result] =
    _ => Future.successful(Ok)

  "Phase6Action" - {
    "must invoke result when phase 6 is enabled" in {
      val app = super
        .guiceApplicationBuilder()
        .configure("feature-flags.phase-6-enabled" -> true)
        .build()

      running(app) {
        val actionProvider = app.injector.instanceOf[Phase6ActionProviderImpl]

        val testRequest = DataRequest(fakeRequest, eoriNumber, emptyUserAnswers)

        val result: Future[Result] = actionProvider.apply().invokeBlock(testRequest, fakeOkResult)

        status(result) mustEqual OK
      }
    }

    "must redirect to page not found when phase 6 is disabled" in {
      val app = super
        .guiceApplicationBuilder()
        .configure("feature-flags.phase-6-enabled" -> false)
        .build()

      running(app) {
        val actionProvider = app.injector.instanceOf[Phase6ActionProviderImpl]

        val testRequest = DataRequest(fakeRequest, eoriNumber, emptyUserAnswers)

        val result: Future[Result] = actionProvider.apply().invokeBlock(testRequest, fakeOkResult)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.ErrorController.notFound().url
      }
    }
  }
}
