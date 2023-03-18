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
import models.reference.{Country, CustomsOffice}
import models.{NormalMode, UnloadingRemarksSentViewModel}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.{CustomsOfficeOfDestinationPage, DateOfPreparationPage}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ReferenceDataService
import views.html.{UnloadingGuidanceView, UnloadingRemarksSentView}
import play.api.inject.bind

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

  "UnloadingRemarksSent Controller" - {
    "return OK and the correct view for a GET when telephone and office name available" in {
      val customsOffice       = CustomsOffice("ID", "NAME", "GB", Some("12345"))
      val officeOfDestination = "CODE-001"
      checkArrivalStatus()
      when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

      setExistingUserAnswers(emptyUserAnswers.setValue(CustomsOfficeOfDestinationPage, officeOfDestination))

      val unloadingRemarksSentViewModel = UnloadingRemarksSentViewModel(Some(customsOffice), officeOfDestination)

      val request = FakeRequest(GET, routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[UnloadingRemarksSentView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(mrn, unloadingRemarksSentViewModel)(request, messages).toString
    }

    "return OK and the correct view for a GET when telephone not available and office name available" in {
      val customsOffice       = CustomsOffice("ID", "NAME", "GB", None)
      val officeOfDestination = "CODE-001"
      checkArrivalStatus()
      when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

      setExistingUserAnswers(emptyUserAnswers.setValue(CustomsOfficeOfDestinationPage, officeOfDestination))

      val unloadingRemarksSentViewModel = UnloadingRemarksSentViewModel(Some(customsOffice), officeOfDestination)

      val request = FakeRequest(GET, routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[UnloadingRemarksSentView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(mrn, unloadingRemarksSentViewModel)(request, messages).toString
    }

    "return OK and the correct view for a GET when telephone available and office name not available" in {
      val customsOffice       = CustomsOffice("ID", "", "GB", Some("12345"))
      val officeOfDestination = "CODE-001"
      checkArrivalStatus()
      when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

      setExistingUserAnswers(emptyUserAnswers.setValue(CustomsOfficeOfDestinationPage, officeOfDestination))

      val unloadingRemarksSentViewModel = UnloadingRemarksSentViewModel(Some(customsOffice), officeOfDestination)

      val request = FakeRequest(GET, routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[UnloadingRemarksSentView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(mrn, unloadingRemarksSentViewModel)(request, messages).toString
    }

    "return OK and the correct view for a GET when telephone not available and office name not available" in {
      val customsOffice       = CustomsOffice("ID", "", "GB", None)
      val officeOfDestination = "CODE-001"
      checkArrivalStatus()
      when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

      setExistingUserAnswers(emptyUserAnswers.setValue(CustomsOfficeOfDestinationPage, officeOfDestination))

      val unloadingRemarksSentViewModel = UnloadingRemarksSentViewModel(Some(customsOffice), officeOfDestination)

      val request = FakeRequest(GET, routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[UnloadingRemarksSentView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(mrn, unloadingRemarksSentViewModel)(request, messages).toString
    }
  }
}
