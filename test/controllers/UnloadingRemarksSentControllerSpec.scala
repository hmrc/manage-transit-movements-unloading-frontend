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
import generated.CustomsOfficeOfDestinationActualType03
import generators.Generators
import models.UnloadingRemarksSentViewModel
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ReferenceDataService
import views.html.UnloadingRemarksSentView

import scala.concurrent.Future

class UnloadingRemarksSentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  private val customsOfficeId = "CODE-001"

  private val customsOffice = CustomsOffice(customsOfficeId, "", "GB", None)

  "UnloadingRemarksSent Controller" - {
    "return OK and the correct view" in {

      checkArrivalStatus()

      when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(customsOffice))

      val ie043Data = basicIe043.copy(CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActualType03(customsOfficeId))
      setExistingUserAnswers(emptyUserAnswers.copy(ie043Data = ie043Data))

      val unloadingRemarksSentViewModel = UnloadingRemarksSentViewModel(customsOffice, "CODE-001")

      val request = FakeRequest(GET, routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[UnloadingRemarksSentView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(mrn, unloadingRemarksSentViewModel)(request, messages).toString
    }

  }
}
