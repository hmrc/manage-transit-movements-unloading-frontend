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
import generated.{CC044CType, CustomsOfficeOfDestinationActualType03}
import generators.Generators
import models.UnloadingRemarksSentViewModel
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ReferenceDataService
import views.html.UnloadingRemarksSentView

import scala.concurrent.Future

class UnloadingRemarksSentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "UnloadingRemarksSent Controller" - {
    "return OK and the correct view" in {
      forAll(arbitrary[CC044CType], nonEmptyString, arbitrary[CustomsOffice]) {
        (cc044cType, customsOfficeId, customsOffice) =>
          beforeEach()

          val transitOperation = cc044cType.TransitOperation.copy(MRN = mrn.value)
          val ie044 = cc044cType.copy(
            TransitOperation = transitOperation,
            CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActualType03(customsOfficeId)
          )

          when(mockUnloadingPermissionMessageService.getIE044(any())(any(), any()))
            .thenReturn(Future.successful(Some(ie044)))

          when(mockReferenceDataService.getCustomsOffice(any())(any()))
            .thenReturn(Future.successful(customsOffice))

          when(mockSessionRepository.remove(any()))
            .thenReturn(Future.successful(true))

          val unloadingRemarksSentViewModel = UnloadingRemarksSentViewModel(customsOffice, customsOfficeId)

          val request = FakeRequest(GET, routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url)

          val result = route(app, request).value

          val view = app.injector.instanceOf[UnloadingRemarksSentView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(mrn.toString, unloadingRemarksSentViewModel)(request, messages).toString

          verify(mockUnloadingPermissionMessageService).getIE044(eqTo(arrivalId))(any(), any())
          verify(mockReferenceDataService).getCustomsOffice(eqTo(customsOfficeId))(any())
          verify(mockSessionRepository).remove(eqTo(arrivalId))
      }
    }

    "must redirect to tech difficulties" - {
      "when IE044 not found" in {
        when(mockUnloadingPermissionMessageService.getIE044(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.ErrorController.technicalDifficulties().url
      }
    }
  }
}
