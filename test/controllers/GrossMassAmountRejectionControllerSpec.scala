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
import forms.GrossMassAmountFormProvider
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnloadingRemarksRejectionService
import views.html.GrossMassAmountRejectionView

import java.time.LocalDate
import scala.concurrent.Future

class GrossMassAmountRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  val formProvider = new GrossMassAmountFormProvider()
  val form         = formProvider()

  lazy val grossMassAmountRejectionRoute = routes.GrossMassAmountRejectionController.onPageLoad(arrivalId).url

  private val mockRejectionService = mock[UnloadingRemarksRejectionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRejectionService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService))

  "GrossMassAmountRejection Controller" - {

    "must populate the view correctly on a GET" in {
      checkArrivalStatus()
      val originalValue = "100000.123"

      when(mockRejectionService.getRejectedValueAsString(any(), any())(any())(any())).thenReturn(Future.successful(Some(originalValue)))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, grossMassAmountRejectionRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[GrossMassAmountRejectionView]

      status(result) mustEqual OK

      contentAsString(result) mustBe
        view(form.fill(originalValue), arrivalId)(request, messages).toString
    }

    "must render the technical difficulties when there is no rejection message" in {
      checkArrivalStatus()
      when(mockRejectionService.getRejectedValueAsString(any(), any())(any())(any())).thenReturn(Future.successful(None))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, grossMassAmountRejectionRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }
  }

  "must redirect to the next page when valid data is submitted" in {
    checkArrivalStatus()
    val originalValue    = "some reference"
    val errors           = Seq(FunctionalError(IncorrectValue, DefaultPointer(""), None, Some(originalValue)))
    val rejectionMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

    when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(rejectionMessage)))
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

    setExistingUserAnswers(emptyUserAnswers)

    val request = FakeRequest(POST, grossMassAmountRejectionRoute)
      .withFormUrlEncodedBody(("value", "123456.123"))

    val result = route(app, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url
  }

  "must render the technical difficulties page when rejection message is None" in {
    checkArrivalStatus()
    when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(None))
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

    setNoExistingUserAnswers()

    val request =
      FakeRequest(POST, grossMassAmountRejectionRoute)
        .withFormUrlEncodedBody(("value", "123456.123"))

    val result = route(app, request).value
    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
  }

  "must return a Bad Request and errors when invalid data is submitted" in {
    checkArrivalStatus()

    setExistingUserAnswers(emptyUserAnswers)

    val request   = FakeRequest(POST, grossMassAmountRejectionRoute).withFormUrlEncodedBody(("value", ""))
    val boundForm = form.bind(Map("value" -> ""))

    val result = route(app, request).value

    val view = app.injector.instanceOf[GrossMassAmountRejectionView]

    status(result) mustEqual BAD_REQUEST

    contentAsString(result) mustBe
      view(boundForm, arrivalId)(request, messages).toString
  }
}
