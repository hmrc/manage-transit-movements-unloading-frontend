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
import models.P5.MessageMetaData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.UnloadingFindingsViewModel
import viewModels.UnloadingFindingsViewModel.UnloadingFindingsViewModelProvider
import views.html.UnloadingFindingsView

import java.time.LocalDateTime
import scala.concurrent.Future

class UnloadingFindingsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockUnloadingFindingsViewModelProvider = mock[UnloadingFindingsViewModelProvider]

  lazy val unloadingFindingsRoute: String = controllers.routes.UnloadingFindingsController.onPageLoad(arrivalId).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingFindingsViewModelProvider].toInstance(mockUnloadingFindingsViewModelProvider))

  "UnloadingFindingsController Controller" - {

    "must return OK and the correct view for a GET" in {
      checkArrivalStatus()
      when(
        mockUnloadingPermissionMessageService
          .getMessageHead(any())(any(), any())
      )
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))

      setExistingUserAnswers(emptyUserAnswers)

      val sections = arbitraryStaticSections.arbitrary.sample.value

      val unloadingFindingsViewModel = UnloadingFindingsViewModel(sections)

      when(mockUnloadingFindingsViewModelProvider.apply(any())(any())).thenReturn(unloadingFindingsViewModel)

      val request = FakeRequest(GET, unloadingFindingsRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[UnloadingFindingsView]

      contentAsString(result) mustEqual
        view(mrn, arrivalId, unloadingFindingsViewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      checkArrivalStatus()

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, unloadingFindingsRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

  }
}
