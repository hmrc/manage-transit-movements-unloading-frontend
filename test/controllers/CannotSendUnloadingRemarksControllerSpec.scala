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
import matchers.JsonMatchers
import models.CannotSendUnloadingRemarksViewModel
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ReferenceDataService
import views.html.CannotSendUnloadingRemarksView

import scala.concurrent.Future

class CannotSendUnloadingRemarksControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers with Generators {

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

  "CannotSendUnloadingRemarksController" - {
    "return OK and the correct view for a GET" in {
      when(mockReferenceDataService.getCustomsOffice(any())(any())).thenReturn(Future.successful(customsOffice))

      val ie043Data = basicIe043.copy(CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActualType03(customsOfficeId))
      setExistingUserAnswers(emptyUserAnswers.copy(ie043Data = ie043Data))

      val request = FakeRequest(GET, routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[CannotSendUnloadingRemarksView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(mrn, arrivalId, CannotSendUnloadingRemarksViewModel(customsOffice, customsOfficeId))(request, messages).toString

      verify(mockReferenceDataService).getCustomsOffice(eqTo(customsOfficeId))(any())
    }
  }
}
