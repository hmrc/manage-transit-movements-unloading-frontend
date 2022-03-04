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
import config.FrontendAppConfig
import forms.DateGoodsUnloadedFormProvider
import matchers.JsonMatchers
import models.{NormalMode, TraderAtDestination, UnloadingPermission}
import navigation.{FakeUnloadingPermissionNavigator, NavigatorUnloadingPermission}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import pages.DateGoodsUnloadedPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.UnloadingPermissionService
import uk.gov.hmrc.viewmodels.{DateInput, NunjucksSupport}

import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.Future

class DateGoodsUnloadedControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  private val stubClock         = Clock.fixed(Instant.now, ZoneId.systemDefault)
  private val dateOfPreparation = LocalDate.now(stubClock)
  private val validAnswer       = dateOfPreparation

  val unloadingPermission = UnloadingPermission(
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
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      setExistingUserAnswers(emptyUserAnswers)

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val viewModel = DateInput.localDate(form("value"))

      val expectedJson = Json.obj(
        "form"        -> form,
        "mrn"         -> mrn,
        "date"        -> viewModel,
        "onSubmitUrl" -> routes.DateGoodsUnloadedController.onSubmit(arrivalId, NormalMode).url
      )

      templateCaptor.getValue mustEqual "dateGoodsUnloaded.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, validAnswer).success.value
      setExistingUserAnswers(userAnswers)

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(
        Map(
          "value.day"   -> validAnswer.getDayOfMonth.toString,
          "value.month" -> validAnswer.getMonthValue.toString,
          "value.year"  -> validAnswer.getYear.toString
        )
      )

      val viewModel = DateInput.localDate(filledForm("value"))

      val expectedJson = Json.obj(
        "form"        -> filledForm,
        "mrn"         -> mrn,
        "date"        -> viewModel,
        "onSubmitUrl" -> routes.DateGoodsUnloadedController.onSubmit(arrivalId, NormalMode).url
      )

      templateCaptor.getValue mustEqual "dateGoodsUnloaded.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return an Internal Server Error on a GET when date of preparation is not available" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

      setExistingUserAnswers(emptyUserAnswers)

      val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
      val templateCaptor    = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor        = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)

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
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      setExistingUserAnswers(emptyUserAnswers)

      val badSubmission = Map(
        "value.day"   -> "invalid value",
        "value.month" -> "invalid value",
        "value.year"  -> "invalid value"
      )
      val request =
        FakeRequest(POST, dateGoodsUnloadedRoute)
          .withFormUrlEncodedBody(badSubmission.toSeq: _*)

      val boundForm = form.bind(badSubmission)

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val viewModel = DateInput.localDate(boundForm("value"))

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "mrn"         -> mrn,
        "date"        -> viewModel,
        "onSubmitUrl" -> routes.DateGoodsUnloadedController.onSubmit(arrivalId, NormalMode).url
      )

      templateCaptor.getValue mustEqual "dateGoodsUnloaded.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return a Bad Request and errors when the date is before date of preparation" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
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

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val viewModel = DateInput.localDate(boundForm("value"))

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "mrn"         -> mrn,
        "date"        -> viewModel,
        "onSubmitUrl" -> routes.DateGoodsUnloadedController.onSubmit(arrivalId, NormalMode).url
      )

      templateCaptor.getValue mustEqual "dateGoodsUnloaded.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return an Internal Server Error when valid data is submitted but date of preparation is not available" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

      setExistingUserAnswers(emptyUserAnswers)

      val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
      val templateCaptor    = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor        = ArgumentCaptor.forClass(classOf[JsObject])

      val postRequest =
        FakeRequest(POST, dateGoodsUnloadedRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )
      val result = route(app, postRequest).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
