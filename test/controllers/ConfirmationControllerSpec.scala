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

import java.time.LocalDate

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.FrontendAppConfig
import matchers.JsonMatchers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.DateGoodsUnloadedPage
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class ConfirmationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers {

  "Confirmation Controller" - {

    "return correct view and remove UserAnswers" in {
      checkArrivalStatus()
      val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, LocalDate.now()).success.value

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(userAnswers)

      val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
      val request           = FakeRequest(GET, routes.ConfirmationController.onPageLoad(arrivalId).url)
      val templateCaptor    = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor        = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
      verify(mockSessionRepository, times(1)).remove(arrivalId)

      val expectedJson = Json.obj("mrn" -> mrn, "manageTransitMovementsUrl" -> frontendAppConfig.viewArrivals)

      templateCaptor.getValue mustEqual "confirmation.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
