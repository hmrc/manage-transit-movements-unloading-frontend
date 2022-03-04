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

import java.time.{Clock, Instant, LocalDate, ZoneId}
import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import config.FrontendAppConfig
import forms.DateGoodsUnloadedFormProvider
import matchers.JsonMatchers
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, TraderAtDestination, UnloadingPermission, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.{UnloadingPermissionService, UnloadingRemarksRejectionService}
import uk.gov.hmrc.viewmodels.{DateInput, NunjucksSupport}

import scala.concurrent.Future

class DateGoodsUnloadedRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val stubClock         = Clock.fixed(Instant.now.plusSeconds(200000), ZoneId.systemDefault)
  val formProvider      = new DateGoodsUnloadedFormProvider(stubClock)
  val dateOfPreparation = LocalDate.now(stubClock)

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

  val unloadingRemarksRejectionMessage = UnloadingRemarksRejectionMessage(
    movementReferenceNumber = mrn,
    rejectionDate = LocalDate.now,
    action = None,
    errors = Seq(FunctionalError(IncorrectValue, DefaultPointer(""), None, Some("some reference")))
  )

  private def form: Form[LocalDate]  = formProvider(dateOfPreparation)
  private val validAnswer: LocalDate = dateOfPreparation

  private lazy val dateGoodsUnloadedRoute = routes.DateGoodsUnloadedRejectionController.onPageLoad(arrivalId).url

  private val mockRejectionService           = mock[UnloadingRemarksRejectionService]
  private val mockUnloadingPermissionService = mock[UnloadingPermissionService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService))
      .overrides(bind[UnloadingPermissionService].toInstance(mockUnloadingPermissionService))
      .overrides(bind[Clock].toInstance(stubClock))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRejectionService, mockUnloadingPermissionService)
  }

  "DateGoodsUnloadedRejectionController" - {

    "must populate the view correctly on a GET" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.getRejectedValueAsDate(any(), any())(any())(any())).thenReturn(Future.successful(Some(validAnswer)))
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      setNoExistingUserAnswers()

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
        "date"        -> viewModel,
        "onSubmitUrl" -> routes.DateGoodsUnloadedRejectionController.onSubmit(arrivalId).url
      )

      templateCaptor.getValue mustEqual "dateGoodsUnloaded.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()
      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(unloadingRemarksRejectionMessage)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      setNoExistingUserAnswers()

      val postRequest = FakeRequest(POST, dateGoodsUnloadedRoute)
        .withFormUrlEncodedBody(
          "value.day"   -> validAnswer.getDayOfMonth.toString,
          "value.month" -> validAnswer.getMonthValue.toString,
          "value.year"  -> validAnswer.getYear.toString
        )

      val result = route(app, postRequest).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url
    }

    "must return an Internal Server Error on a GET when date of preparation is not available" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(unloadingRemarksRejectionMessage)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

      setNoExistingUserAnswers()

      val result = route(app, FakeRequest(GET, dateGoodsUnloadedRoute)).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
      val templateCaptor    = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor        = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"

      val expectedJson = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(unloadingRemarksRejectionMessage)))
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      setExistingUserAnswers(emptyUserAnswers)

      val badSubmission = Map(
        "value.day"   -> "invalid value",
        "value.month" -> "invalid value",
        "value.year"  -> "invalid value"
      )

      val postRequest = FakeRequest(POST, dateGoodsUnloadedRoute)
        .withFormUrlEncodedBody(badSubmission.toSeq: _*)

      val boundForm      = form.bind(badSubmission)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, postRequest).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val viewModel = DateInput.localDate(boundForm("value"))

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "date"        -> viewModel,
        "onSubmitUrl" -> routes.DateGoodsUnloadedRejectionController.onSubmit(arrivalId).url
      )

      templateCaptor.getValue mustEqual "dateGoodsUnloaded.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return a Bad Request and errors when the date is before date of preparation" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(unloadingRemarksRejectionMessage)))
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

      setExistingUserAnswers(emptyUserAnswers)

      val invalidDate = dateOfPreparation.minusDays(1)

      val badSubmission = Map(
        "value.day"   -> invalidDate.getDayOfMonth.toString,
        "value.month" -> invalidDate.getMonth.toString,
        "value.year"  -> invalidDate.getYear.toString
      )

      val postRequest = FakeRequest(POST, dateGoodsUnloadedRoute)
        .withFormUrlEncodedBody(badSubmission.toSeq: _*)

      val boundForm      = form.bind(badSubmission)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, postRequest).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val viewModel = DateInput.localDate(boundForm("value"))

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "date"        -> viewModel,
        "onSubmitUrl" -> routes.DateGoodsUnloadedRejectionController.onSubmit(arrivalId).url
      )

      templateCaptor.getValue mustEqual "dateGoodsUnloaded.njk"
      jsonCaptor.getValue must containJson(expectedJson)

    }

    "must return an Internal Server Error when valid data is submitted but date of preparation is not available" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(unloadingRemarksRejectionMessage)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

      setNoExistingUserAnswers()

      val invalidDate = dateOfPreparation.minusDays(1)

      val badSubmission = Map(
        "value.day"   -> invalidDate.getDayOfMonth.toString,
        "value.month" -> invalidDate.getMonth.toString,
        "value.year"  -> invalidDate.getYear.toString
      )

      val postRequest = FakeRequest(POST, dateGoodsUnloadedRoute)
        .withFormUrlEncodedBody(badSubmission.toSeq: _*)

      val result = route(app, postRequest).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
      val templateCaptor    = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor        = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"

      val expectedJson = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
