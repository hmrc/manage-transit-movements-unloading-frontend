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
import models.NormalMode
import models.P5.ArrivalMessageType.UnloadingPermission
import models.P5.{ArrivalMessageType, MessageMetaData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.HouseConsignmentViewModel
import viewModels.HouseConsignmentViewModel.HouseConsignmentViewModelProvider
import views.html.HouseConsignmentView
import org.scalacheck.Arbitrary.arbitrary

import java.time.LocalDateTime
import scala.concurrent.Future

class HouseConsignmentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockHouseConsignmentViewModelProvider = mock[HouseConsignmentViewModelProvider]

  lazy val houseConsignmentRoute: String = controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, index).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[HouseConsignmentViewModelProvider].toInstance(mockHouseConsignmentViewModelProvider))

  "UnloadingFindingsController Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setExistingUserAnswers(emptyUserAnswers)

      val sections = arbitrarySections.arbitrary.sample.value

      when(mockHouseConsignmentViewModelProvider.apply(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HouseConsignmentViewModel(sections, sections)))

      val houseConsignmentViewModel = HouseConsignmentViewModel(sections, sections)

      val request = FakeRequest(GET, houseConsignmentRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[HouseConsignmentView]

      contentAsString(result) mustEqual
        view(mrn, arrivalId, houseConsignmentViewModel, index)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, houseConsignmentRoute)

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

      val request = FakeRequest(GET, routes.HouseConsignmentController.onPageLoad(arrivalId, index).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId).url

    }
  }
}
