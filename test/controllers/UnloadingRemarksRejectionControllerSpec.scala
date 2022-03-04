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
import generators.MessagesModelGenerators
import matchers.JsonMatchers
import models.{FunctionalError, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.UnloadingRemarksRejectionService

import scala.concurrent.Future

class UnloadingRemarksRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers with MessagesModelGenerators {

  private val mockUnloadingRemarksRejectionService = mock[UnloadingRemarksRejectionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnloadingRemarksRejectionService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockUnloadingRemarksRejectionService))

  "UnloadingRemarksRejection Controller" - {

    "return OK and the single error rejection view for a GET when unloading rejection message returns a Some" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val functionalError = arbitrary[FunctionalError](arbitraryRejectionErrorNonDefaultPointer).sample.value

      val errors: Seq[FunctionalError] = Seq(functionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "unloadingRemarksRejection.njk"
    }

    "return OK and the multiple error rejection view for a GET when unloading rejection message returns Some" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val functionalError = arbitrary[FunctionalError](arbitraryRejectionError).sample.value

      val errors = Seq(functionalError, functionalError)

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors))))

      setExistingUserAnswers(emptyUserAnswers)

      val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
      val request           = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)
      val templateCaptor    = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor        = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      val expectedJson =
        Json.obj(
          "errors"                     -> errors,
          "contactUrl"                 -> frontendAppConfig.nctsEnquiriesUrl,
          "declareUnloadingRemarksUrl" -> routes.IndexController.onPageLoad(arrivalId).url
        )
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "unloadingRemarksMultipleErrorsRejection.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "render 'Technical difficulties' page when unloading rejection message's has no errors" in {

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, Seq.empty))))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
    }

    "render the 'Technical difficulties' page when unloading rejection message returns a None" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any()))
        .thenReturn(Future.successful(None))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      verify(mockUnloadingRemarksRejectionService, times(1)).unloadingRemarksRejectionMessage(any())(any())
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
    }
  }
}
