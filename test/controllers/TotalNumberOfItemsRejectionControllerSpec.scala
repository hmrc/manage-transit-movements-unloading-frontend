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
import forms.TotalNumberOfItemsFormProvider
import matchers.JsonMatchers
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import pages.TotalNumberOfItemsPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class TotalNumberOfItemsRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val formProvider = new TotalNumberOfItemsFormProvider()
  val form         = formProvider()

  val validAnswer = 1

  lazy val totalNumberOfItemsRoute = routes.TotalNumberOfItemsRejectionController.onPageLoad(arrivalId).url

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

  "TotalNumberOfItems Controller" - {

    "must populate the view correctly on a GET when the question has previously been answered" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockRejectionService.getRejectedValueAsInt(any(), any())(any())(any())).thenReturn(Future.successful(Some(validAnswer)))

      setNoExistingUserAnswers()

      val request        = FakeRequest(GET, totalNumberOfItemsRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])
      val result         = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> validAnswer.toString))

      val expectedJson = Json.obj(
        "form"        -> filledForm,
        "onSubmitUrl" -> routes.TotalNumberOfItemsRejectionController.onSubmit(arrivalId).url
      )

      templateCaptor.getValue mustEqual "totalNumberOfItems.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must render the Technical Difficulties page when get rejected value is None" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.getRejectedValueAsInt(any(), any())(any())(any())).thenReturn(Future.successful(None))

      val userAnswers = emptyUserAnswers.set(TotalNumberOfItemsPage, validAnswer).success.value
      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, totalNumberOfItemsRoute)
      val result         = route(app, request).value
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {
      checkArrivalStatus()
      val originalValue    = "some reference"
      val errors           = Seq(FunctionalError(IncorrectValue, DefaultPointer(""), None, Some(originalValue)))
      val rejectionMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(rejectionMessage)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, totalNumberOfItemsRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(POST, totalNumberOfItemsRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm      = form.bind(Map("value" -> "invalid value"))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "onSubmitUrl" -> routes.TotalNumberOfItemsRejectionController.onSubmit(arrivalId).url
      )

      templateCaptor.getValue mustEqual "totalNumberOfItems.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must render Technical Difficulties when there is no rejection message on submission" in {
      checkArrivalStatus()
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(None))

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, totalNumberOfItemsRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj("contactUrl" -> frontendAppConfig.nctsEnquiriesUrl)

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
