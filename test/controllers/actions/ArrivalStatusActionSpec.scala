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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.EoriNumber
import models.P5.ArrivalMessageType.UnloadingPermission
import models.P5.{ArrivalMessageType, MessageMetaData}
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Results._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArrivalStatusActionSpec extends SpecBase with BeforeAndAfterEach with Generators with AppWithDefaultMockFixtures {

  private def fakeOkResult[A]: A => Future[Result] =
    _ => Future.successful(Ok)

  "ArrivalStatusAction" - {
    "must return None when an unloading permission is available" in {
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), UnloadingPermission, ""))))

      val checkArrivalStatusProvider = new CheckArrivalStatusProvider(mockUnloadingPermissionMessageService)

      val testRequest = IdentifierRequest(FakeRequest(GET, "/"), EoriNumber("eori"))

      val result: Future[Result] = checkArrivalStatusProvider.apply(arrivalId).invokeBlock(testRequest, fakeOkResult)

      status(result) mustEqual OK
    }

    "must return 303 and redirect to CannotSendUnloadingRemarks when no unloading permission is available" in {

      val messageType = arbitrary[ArrivalMessageType].retryUntil(_ != UnloadingPermission).sample.value
      when(mockUnloadingPermissionMessageService.getMessageHead(any())(any(), any()))
        .thenReturn(Future.successful(Some(MessageMetaData(LocalDateTime.now(), messageType, ""))))

      val checkArrivalStatusProvider = new CheckArrivalStatusProvider(mockUnloadingPermissionMessageService)

      val testRequest = IdentifierRequest(FakeRequest(GET, "/"), EoriNumber("eori"))

      val result: Future[Result] = checkArrivalStatusProvider.apply(arrivalId).invokeBlock(testRequest, fakeOkResult)

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.CannotSendUnloadingRemarksController.onPageLoad(arrivalId).url
    }
  }
}
