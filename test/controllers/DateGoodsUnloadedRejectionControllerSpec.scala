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
import forms.DateGoodsUnloadedFormProvider
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.{DateGoodsUnloadedPage, DateOfPreparationPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.DateGoodsUnloadedRejectionView

import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.Future

class DateGoodsUnloadedRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val stubClock         = Clock.fixed(Instant.now, ZoneId.systemDefault)
  private val formProvider      = new DateGoodsUnloadedFormProvider(stubClock)
  private val dateOfPreparation = LocalDate.now(stubClock).minusWeeks(1)
  private val validAnswer       = dateOfPreparation

  private def form: Form[LocalDate] = formProvider(dateOfPreparation)

  private lazy val dateGoodsUnloadedRoute = routes.DateGoodsUnloadedRejectionController.onPageLoad(arrivalId).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[Clock].toInstance(stubClock))

  "DateGoodsUnloadedRejectionController" - {

    "must populate the view correctly on a GET" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers
        .setValue(DateOfPreparationPage, dateOfPreparation)
        .setValue(DateGoodsUnloadedPage, validAnswer)
      setExistingUserAnswers(userAnswers)

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual OK

      val filledForm = form.bind(
        Map(
          "value.day"   -> validAnswer.getDayOfMonth.toString,
          "value.month" -> validAnswer.getMonthValue.toString,
          "value.year"  -> validAnswer.getYear.toString
        )
      )

      val view    = injector.instanceOf[DateGoodsUnloadedRejectionView]
      val request = FakeRequest(GET, dateGoodsUnloadedRoute)

      contentAsString(result) mustBe view(mrn.toString, arrivalId, filledForm)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(DateOfPreparationPage, dateOfPreparation)
      setExistingUserAnswers(userAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val postRequest = FakeRequest(POST, dateGoodsUnloadedRoute)
        .withFormUrlEncodedBody(
          "value.day"   -> validAnswer.getDayOfMonth.toString,
          "value.month" -> validAnswer.getMonthValue.toString,
          "value.year"  -> validAnswer.getYear.toString
        )

      val result = route(app, postRequest).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.get(DateGoodsUnloadedPage).get mustBe validAnswer
    }

    "must redirect to session expired when date of preparation is not available" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(DateGoodsUnloadedPage, validAnswer)
      setExistingUserAnswers(userAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to session expired when date goods unloaded is not available" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(DateOfPreparationPage, dateOfPreparation)
      setExistingUserAnswers(userAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(DateOfPreparationPage, dateOfPreparation)
      setExistingUserAnswers(userAnswers)

      val badSubmission = Map(
        "value.day"   -> "invalid value",
        "value.month" -> "invalid value",
        "value.year"  -> "invalid value"
      )

      val postRequest = FakeRequest(POST, dateGoodsUnloadedRoute)
        .withFormUrlEncodedBody(badSubmission.toSeq: _*)

      val boundForm = form.bind(badSubmission)
      val result    = route(app, postRequest).value

      status(result) mustEqual BAD_REQUEST

      val view    = injector.instanceOf[DateGoodsUnloadedRejectionView]
      val request = FakeRequest(GET, dateGoodsUnloadedRoute)

      contentAsString(result) mustBe view(mrn.toString, arrivalId, boundForm)(request, messages).toString
    }

    "must return a Bad Request and errors when the date is before date of preparation" in {
      checkArrivalStatus()

      val userAnswers = emptyUserAnswers.setValue(DateOfPreparationPage, dateOfPreparation)
      setExistingUserAnswers(userAnswers)

      val invalidDate = dateOfPreparation.minusDays(1)

      val badSubmission = Map(
        "value.day"   -> invalidDate.getDayOfMonth.toString,
        "value.month" -> invalidDate.getMonth.toString,
        "value.year"  -> invalidDate.getYear.toString
      )

      val postRequest = FakeRequest(POST, dateGoodsUnloadedRoute)
        .withFormUrlEncodedBody(badSubmission.toSeq: _*)

      val boundForm = form.bind(badSubmission)
      val result    = route(app, postRequest).value

      status(result) mustEqual BAD_REQUEST

      val view    = injector.instanceOf[DateGoodsUnloadedRejectionView]
      val request = FakeRequest(GET, dateGoodsUnloadedRoute)

      contentAsString(result) mustBe view(mrn.toString, arrivalId, boundForm)(request, messages).toString
    }
  }
}
