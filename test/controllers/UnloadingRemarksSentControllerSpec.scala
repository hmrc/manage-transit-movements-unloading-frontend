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
import models.P5.ArrivalMessageType.UnloadingPermission
import models.P5.{ArrivalMessageType, MessageMetaData}
import models.UnloadingRemarksSentViewModel
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ReferenceDataService
import views.html.UnloadingRemarksSentView

import java.time.LocalDateTime
import scala.concurrent.Future

class UnloadingRemarksSentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]
  private val customsOffice                          = CustomsOffice("ID", "", "GB", None)

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

  "UnloadingRemarksSent Controller" - {
    "return OK and the correct view" in {

      checkArrivalStatus()

      when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

      setExistingUserAnswers(emptyUserAnswers.copy(ie043Data = setCustomsOfficeOfDestination))

      val unloadingRemarksSentViewModel = UnloadingRemarksSentViewModel(Some(customsOffice), "CODE-001")

      val request = FakeRequest(GET, routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[UnloadingRemarksSentView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(mrn, unloadingRemarksSentViewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no CustomsOfficeOfDestination data is found" in {
      checkArrivalStatus()

      setExistingUserAnswers(emptyUserAnswers)

      when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

      val request = FakeRequest(GET, routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "return OK and the correct view for a GET when message is not Unloading Permission(IE043)" in {
      checkArrivalStatus()
      val messageType = arbitrary[ArrivalMessageType].retryUntil(_ != UnloadingPermission).sample.value
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), messageType, ""))))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId).url

    }
  }
}
