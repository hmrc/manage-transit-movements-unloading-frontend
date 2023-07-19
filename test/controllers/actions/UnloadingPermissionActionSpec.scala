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
import controllers.routes
import models.P5._
import models.requests.IdentifierRequest
import models.{EoriNumber, MovementReferenceNumber}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Results._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.P5.UnloadingPermissionMessageService

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingPermissionActionSpec extends SpecBase with BeforeAndAfterEach with AppWithDefaultMockFixtures {

  val mockService: UnloadingPermissionMessageService = mock[UnloadingPermissionMessageService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockService)
  }

  private def fakeOkResult[A]: A => Future[Result] =
    _ => Future.successful(Ok)

  "UnloadingPermissionAction" - {
    "must return 200 when an unloading permission is available" in {

      val message = IE043Data(
        MessageData(
          preparationDateAndTime = LocalDateTime.now(),
          TransitOperation = TransitOperation(MovementReferenceNumber("23", "GB", "123")),
          Consignment = Consignment(None, None, List.empty),
          CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActual("GB0008"),
          TraderAtDestination = TraderAtDestination("AB123")
        )
      )

      when(mockService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(message)))

      val unloadingPermissionProvider = (new UnloadingPermissionActionProvider(mockService)(implicitly))(arrivalId)

      val testRequest = IdentifierRequest(FakeRequest(GET, "/"), EoriNumber("eori"))

      val result: Future[Result] = unloadingPermissionProvider.invokeBlock(testRequest, fakeOkResult)

      status(result) mustEqual OK
    }

    "must return 303 and redirect to technical difficulties when no unloading permission is available" in {

      when(mockService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

      val unloadingPermissionProvider = (new UnloadingPermissionActionProvider(mockService)(implicitly))(arrivalId)

      val testRequest = IdentifierRequest(FakeRequest(GET, "/"), EoriNumber("eori"))

      val result: Future[Result] = unloadingPermissionProvider.invokeBlock(testRequest, fakeOkResult)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe routes.ErrorController.technicalDifficulties().url
    }
  }
}
