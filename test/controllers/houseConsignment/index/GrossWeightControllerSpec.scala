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
import forms.WeightFormProvider
import generators.Generators
import models.NormalMode
import navigation.houseConsignment.index.HouseConsignmentNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.houseConsignment.index.GrossWeightPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.houseConsignment.index.GrossWeightView

import scala.concurrent.Future

class GrossWeightControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {
  private val decimalPlace   = notTooBigPositiveNumbers.sample.value
  private val characterCount = notTooBigPositiveNumbers.sample.value
  private val formProvider   = new WeightFormProvider()
  private val mode           = NormalMode

  private val form =
    formProvider(s"houseConsignment.index.grossWeight.$mode", decimalPlace, characterCount, isZeroAllowed = false, houseConsignmentIndex.display)
  private val validAnswer = BigDecimal(123.45)

  private lazy val grossWeightAmountRoute =
    controllers.houseConsignment.index.routes.GrossWeightController.onPageLoad(arrivalId, index, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[HouseConsignmentNavigator].toInstance(FakeHouseConsignmentNavigators.fakeHouseConsignmentNavigator(frontendAppConfig))
      )

  "GrossWeightAmount Controller" - {

    "must return OK and the correct view for a GET" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, grossWeightAmountRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[GrossWeightView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.setValue(GrossWeightPage(index), validAnswer)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, grossWeightAmountRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> validAnswer.toString))

      val view = injector.instanceOf[GrossWeightView]

      contentAsString(result) mustEqual
        view(filledForm, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, grossWeightAmountRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, grossWeightAmountRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      val view = injector.instanceOf[GrossWeightView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must return a Bad Request and errors when 0 is submitted" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, grossWeightAmountRoute).withFormUrlEncodedBody(("value", "0"))
      val boundForm = form.bind(Map("value" -> "0"))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      val view = injector.instanceOf[GrossWeightView]

      contentAsString(result) mustEqual
        view(boundForm, mrn, arrivalId, index, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, grossWeightAmountRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, grossWeightAmountRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
