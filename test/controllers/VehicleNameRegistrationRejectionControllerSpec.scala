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
import forms.VehicleNameRegistrationReferenceFormProvider
import generators.MessagesModelGenerators
import matchers.JsonMatchers
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, UnloadingRemarksRejectionMessage, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class VehicleNameRegistrationRejectionControllerSpec
    extends SpecBase
    with AppWithDefaultMockFixtures
    with NunjucksSupport
    with JsonMatchers
    with MessagesModelGenerators {

  val formProvider = new VehicleNameRegistrationReferenceFormProvider()
  val form         = formProvider()

  lazy val vehicleNameRegistrationRejectionRoute: String = routes.VehicleNameRegistrationRejectionController.onPageLoad(arrivalId).url

  private val mockRejectionService = mock[UnloadingRemarksRejectionService]
  private val frontendAppConfig    = app.injector.instanceOf[FrontendAppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRejectionService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService))

  "VehicleNameRegistrationRejectionController Controller" - {

    "must populate the value from the rejection service original value attribute" in {
      checkArrivalStatus()
      val originalValue = "some reference"

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.getRejectedValueAsString(any(), any())(any())(any())).thenReturn(Future.successful(Some(originalValue)))
      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(GET, vehicleNameRegistrationRejectionRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])
      val result         = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> originalValue))
      val expectedJson = Json.obj(
        "form"        -> filledForm,
        "onSubmitUrl" -> routes.VehicleNameRegistrationRejectionController.onSubmit(arrivalId).url
      )

      templateCaptor.getValue mustEqual "vehicleNameRegistrationReference.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must render the technical difficulties page when there is no rejection message" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.getRejectedValueAsString(any(), any())(any())(any())).thenReturn(Future.successful(None))

      setExistingUserAnswers(emptyUserAnswers)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val request = FakeRequest(GET, vehicleNameRegistrationRejectionRoute)

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(POST, vehicleNameRegistrationRejectionRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "onSubmitUrl" -> routes.VehicleNameRegistrationRejectionController.onSubmit(arrivalId).url
      )

      templateCaptor.getValue mustEqual "vehicleNameRegistrationReference.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to check your answers page for a POST" in {
      checkArrivalStatus()
      val originalValue     = "some reference"
      val errors            = Seq(FunctionalError(IncorrectValue, DefaultPointer(""), None, Some(originalValue)))
      val rejectionMessage  = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(rejectionMessage)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, vehicleNameRegistrationRejectionRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())

      userAnswersCaptor.getValue.data mustBe Json.obj("vehicleNameRegistrationReference" -> "answer")
      userAnswersCaptor.getValue.id mustBe arrivalId
      userAnswersCaptor.getValue.mrn mustBe mrn
    }

    "must render the technical difficulties page for a POST" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(None))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setNoExistingUserAnswers()
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val request =
        FakeRequest(POST, vehicleNameRegistrationRejectionRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
