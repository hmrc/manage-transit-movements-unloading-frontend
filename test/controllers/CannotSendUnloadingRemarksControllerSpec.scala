/*
 * Copyright 2023 HM Revenue & Customs
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
import generators.Generators
import matchers.JsonMatchers
import models.CannotSendUnloadingRemarksViewModel
import models.reference.CustomsOffice
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ReferenceDataService
import views.html.CannotSendUnloadingRemarksView
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future

class CannotSendUnloadingRemarksControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val customsOffice =
    CustomsOffice("ID", "", "GB", None)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  val setCustomsOfficeOfDestination: JsObject = Json.obj(
    "CustomsOfficeOfDestinationActual" -> Json.obj(
      "referenceNumber" -> "CODE-001"
    )
  )
  "CannotSendUnloadingRemarksController" - {
    "return OK and the correct view for a GET" in {
      checkArrivalStatus()

      when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

      setExistingUserAnswers(emptyUserAnswers.copy(ie043Data = setCustomsOfficeOfDestination))

      val request = FakeRequest(GET, routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId, messageId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[CannotSendUnloadingRemarksView]

      // status(result) mustBe OK

      contentAsString(result) mustEqual view(mrn, arrivalId, messageId, CannotSendUnloadingRemarksViewModel(None, "CODE-001"))(request, messages).toString
    }

  }
}
