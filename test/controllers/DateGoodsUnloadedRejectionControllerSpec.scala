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
import generators.Generators
import models.{UnloadingPermission, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.{DateGoodsUnloadedPage, DateOfPreparationPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnloadingPermissionService
import views.html.DateGoodsUnloadedRejectionView

import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.Future

class DateGoodsUnloadedRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val stubClock         = Clock.fixed(Instant.now, ZoneId.systemDefault)
  private val formProvider      = new DateGoodsUnloadedFormProvider(stubClock)
  private val validAnswer       = LocalDate.now(stubClock)
  private val dateOfPreparation = validAnswer.minusWeeks(1)

  private val unloadingPermission = arbitrary[UnloadingPermission].sample.value.copy(
    movementReferenceNumber = mrn.toString,
    dateOfPreparation = dateOfPreparation
  )

  private def form: Form[LocalDate] = formProvider(dateOfPreparation)

  private lazy val dateGoodsUnloadedRoute = routes.DateGoodsUnloadedRejectionController.onPageLoad(arrivalId).url

  private val mockUnloadingPermissionService = mock[UnloadingPermissionService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingPermissionService].toInstance(mockUnloadingPermissionService))
      .overrides(bind[Clock].toInstance(stubClock))

  "DateGoodsUnloadedRejectionController" - {

    "must populate the view correctly on a GET when the value has not been extracted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual OK

      val view = injector.instanceOf[DateGoodsUnloadedRejectionView]

      val request = FakeRequest(GET, dateGoodsUnloadedRoute)

      contentAsString(result) mustBe view(mrn.toString, arrivalId, form)(request, messages).toString
    }

    "must populate the view correctly on a GET when the value has been extracted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers.setValue(DateGoodsUnloadedPage, validAnswer))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual OK

      val filledForm = form.bind(
        Map(
          "value.day"   -> validAnswer.getDayOfMonth.toString,
          "value.month" -> validAnswer.getMonthValue.toString,
          "value.year"  -> validAnswer.getYear.toString
        )
      )

      val view = injector.instanceOf[DateGoodsUnloadedRejectionView]

      val request = FakeRequest(GET, dateGoodsUnloadedRoute)

      contentAsString(result) mustBe view(mrn.toString, arrivalId, filledForm)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

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

    "must return an Internal Server Error on a GET when date of preparation is not available" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers.setValue(DateGoodsUnloadedPage, validAnswer))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }

    "must return an Internal Server Error on a POST when date of preparation is not available" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers.setValue(DateGoodsUnloadedPage, validAnswer))

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

      val result = route(app, FakeRequest(POST, dateGoodsUnloadedRoute)).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

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
