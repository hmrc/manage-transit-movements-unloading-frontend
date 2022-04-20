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
import cats.data.NonEmptyList
import forms.DateGoodsUnloadedFormProvider
import models.{NormalMode, TraderAtDestination, UnloadingPermission}
import navigation.{FakeUnloadingPermissionNavigator, NavigatorUnloadingPermission}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.DateGoodsUnloadedPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnloadingPermissionService
import views.html.DateGoodsUnloadedView

import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.Future

class DateGoodsUnloadedControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val stubClock         = Clock.fixed(Instant.now, ZoneId.systemDefault)
  private val dateOfPreparation = LocalDate.now(stubClock)
  private val validAnswer       = dateOfPreparation

  private val unloadingPermission = UnloadingPermission(
    movementReferenceNumber = "19IT02110010007827",
    transportIdentity = None,
    transportCountry = None,
    grossMass = "1000",
    numberOfItems = 1,
    numberOfPackages = Some(1),
    traderAtDestination = TraderAtDestination("eori", "name", "streetAndNumber", "postcode", "city", "countryCode"),
    presentationOffice = "GB000060",
    seals = None,
    goodsItems = NonEmptyList(goodsItemMandatory, Nil),
    dateOfPreparation = dateOfPreparation
  )

  private def form = new DateGoodsUnloadedFormProvider(stubClock)(dateOfPreparation)

  private lazy val dateGoodsUnloadedRoute = routes.DateGoodsUnloadedController.onPageLoad(arrivalId, NormalMode).url

  private val mockUnloadingPermissionService = mock[UnloadingPermissionService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[NavigatorUnloadingPermission].toInstance(new FakeUnloadingPermissionNavigator(onwardRoute)),
        bind[UnloadingPermissionService].toInstance(mockUnloadingPermissionService),
        bind[Clock].toInstance(stubClock)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingPermissionService)
  }

  "DateGoodsUnloaded Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      setExistingUserAnswers(emptyUserAnswers)

      val view = app.injector.instanceOf[DateGoodsUnloadedView]

      val request = FakeRequest(GET, dateGoodsUnloadedRoute)

      val result = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe view(mrn, arrivalId, NormalMode, form)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, validAnswer).success.value

      setExistingUserAnswers(userAnswers)

      val view = app.injector.instanceOf[DateGoodsUnloadedView]

      val request = FakeRequest(GET, dateGoodsUnloadedRoute)

      val result = route(app, request).value

      val filledForm = form.bind(
        Map(
          "value.day"   -> validAnswer.getDayOfMonth.toString,
          "value.month" -> validAnswer.getMonthValue.toString,
          "value.year"  -> validAnswer.getYear.toString
        )
      )

      status(result) mustEqual OK
      contentAsString(result) mustBe view(mrn, arrivalId, NormalMode, filledForm)(request, messages).toString
    }

    "must return an Internal Server Error on a GET when date of preparation is not available" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

      setExistingUserAnswers(emptyUserAnswers)

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url

    }

    "must redirect on to the next page when valid data is submitted" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val postRequest =
        FakeRequest(POST, dateGoodsUnloadedRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )

      val result = route(app, postRequest).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      setExistingUserAnswers(emptyUserAnswers)

      val badSubmission = Map(
        "value.day"   -> "invalid value",
        "value.month" -> "invalid value",
        "value.year"  -> "invalid value"
      )

      val view = app.injector.instanceOf[DateGoodsUnloadedView]

      val request =
        FakeRequest(POST, dateGoodsUnloadedRoute)
          .withFormUrlEncodedBody(badSubmission.toSeq: _*)

      val boundForm = form.bind(badSubmission)

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustBe view(mrn, arrivalId, NormalMode, boundForm)(request, messages).toString
    }

    "must return a Bad Request and errors when the date is before date of preparation" in {
      checkArrivalStatus()

      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      setExistingUserAnswers(emptyUserAnswers)

      val invalidDate = dateOfPreparation.minusDays(1)

      val badSubmission = Map(
        "value.day"   -> invalidDate.getDayOfMonth.toString,
        "value.month" -> invalidDate.getMonth.toString,
        "value.year"  -> invalidDate.getYear.toString
      )
      val request =
        FakeRequest(POST, dateGoodsUnloadedRoute)
          .withFormUrlEncodedBody(badSubmission.toSeq: _*)

      val boundForm = form.bind(badSubmission)

      val view = app.injector.instanceOf[DateGoodsUnloadedView]

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustBe view(mrn, arrivalId, NormalMode, boundForm)(request, messages).toString
    }

    "must return an Internal Server Error when valid data is submitted but date of preparation is not available" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

      setExistingUserAnswers(emptyUserAnswers)

      val postRequest =
        FakeRequest(POST, dateGoodsUnloadedRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )
      val result = route(app, postRequest).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
    }
  }
}
