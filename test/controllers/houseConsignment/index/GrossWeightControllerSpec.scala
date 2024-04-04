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

package controllers.houseConsignment.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.GrossWeightFormProvider
import generators.Generators
import models.{CheckMode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.houseConsignment.index.GrossWeightPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.GrossWeightView

import scala.concurrent.Future

class GrossWeightControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {
  private val decimalPlace   = positiveInts.sample.value
  private val characterCount = positiveInts.sample.value
  private val formProvider   = new GrossWeightFormProvider()
  private val form           = formProvider("houseConsignment.index.grossWeight", decimalPlace, characterCount, houseConsignmentIndex.display)
  private val mode           = NormalMode
  private val checkMode      = CheckMode
  private val validAnswer    = BigDecimal(123.45)

  private lazy val GrossWeightAmountRoute =
    controllers.houseConsignment.index.routes.GrossWeightController.onPageLoad(arrivalId, index, mode).url

  private lazy val GrossWeightAmountRouteCheckMode =
    controllers.houseConsignment.index.routes.GrossWeightController.onPageLoad(arrivalId, index, checkMode).url

  "GrossWeightAmount Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, GrossWeightAmountRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[GrossWeightView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(GrossWeightPage(index), validAnswer)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, GrossWeightAmountRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> validAnswer.toString))

      val view = injector.instanceOf[GrossWeightView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, GrossWeightAmountRouteCheckMode)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, GrossWeightAmountRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      val view = injector.instanceOf[GrossWeightView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, GrossWeightAmountRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, GrossWeightAmountRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
