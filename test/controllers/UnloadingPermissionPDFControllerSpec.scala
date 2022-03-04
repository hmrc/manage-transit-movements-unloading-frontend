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

import akka.util.ByteString
import base.{AppWithDefaultMockFixtures, SpecBase}
import config.FrontendAppConfig
import connectors.UnloadingConnector
import generators.Generators
import matchers.JsonMatchers.containJson
import models.ArrivalId
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class UnloadingPermissionPDFControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  private val wsResponse: AhcWSResponse = mock[AhcWSResponse]
  val mockUnloadingConnector            = mock[UnloadingConnector]
  private val frontendAppConfig         = app.injector.instanceOf[FrontendAppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(wsResponse)
    reset(mockUnloadingConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingConnector].toInstance(mockUnloadingConnector))

  "UnloadingPermissionPDFController" - {

    "getPDF" - {

      "must return OK and PDF" in {

        val pdfAsBytes: Array[Byte] = Seq.fill(10)(Byte.MaxValue).toArray

        val expectedHeaders = Map(CONTENT_TYPE -> Seq("application/pdf"), CONTENT_DISPOSITION -> Seq("unloading_permission_123"), "OtherHeader" -> Seq("value"))

        when(wsResponse.status) thenReturn 200
        when(wsResponse.bodyAsBytes) thenReturn ByteString(pdfAsBytes)
        when(wsResponse.headers) thenReturn expectedHeaders

        when(mockUnloadingConnector.getPDF(any(), any())(any()))
          .thenReturn(Future.successful(wsResponse))

        val arrivalId = ArrivalId(0)

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, routes.UnloadingPermissionPDFController.getPDF(arrivalId).url)
          .withSession(("authToken" -> "BearerToken"))

        val result = route(app, request).value

        status(result) mustEqual OK
        headers(result).get(CONTENT_TYPE).value mustEqual "application/pdf"
        headers(result).get(CONTENT_DISPOSITION).value mustBe "unloading_permission_123"
      }

      "must redirect to UnauthorisedController if bearer token is missing" in {

        val arrivalId = ArrivalId(0)

        setNoExistingUserAnswers()

        val request = FakeRequest(
          GET,
          routes.UnloadingPermissionPDFController.getPDF(arrivalId).url
        )

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnauthorisedController.onPageLoad().url
      }

      "must render the TechnicalDifficulties page if connector returns error" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val genErrorResponseCode = Gen.oneOf(300, 500).sample.value

        when(wsResponse.status) thenReturn genErrorResponseCode

        when(mockUnloadingConnector.getPDF(any(), any())(any()))
          .thenReturn(Future.successful(wsResponse))

        val arrivalId = ArrivalId(0)

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, routes.UnloadingPermissionPDFController.getPDF(arrivalId).url)
          .withSession(("authToken" -> "BearerToken"))
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
}
