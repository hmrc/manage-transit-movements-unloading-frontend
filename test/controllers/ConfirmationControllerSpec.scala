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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.DateGoodsUnloadedPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ConfirmationView

import java.time.LocalDate
import scala.concurrent.Future

class ConfirmationControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  "Confirmation Controller" - {

    "return correct view and remove UserAnswers" in {
      checkArrivalStatus()
      val userAnswers = emptyUserAnswers.setValue(DateGoodsUnloadedPage, LocalDate.now())

      setExistingUserAnswers(userAnswers)

      when(mockSessionRepository.remove(any())).thenReturn(Future.successful(()))

      val request = FakeRequest(GET, routes.ConfirmationController.onPageLoad(arrivalId).url)
      val result  = route(app, request).value
      val view    = injector.instanceOf[ConfirmationView]

      status(result) mustEqual OK

      verify(mockSessionRepository, times(1)).remove(arrivalId)

      contentAsString(result) mustEqual view(mrn)(request, messages).toString
    }
  }
}
