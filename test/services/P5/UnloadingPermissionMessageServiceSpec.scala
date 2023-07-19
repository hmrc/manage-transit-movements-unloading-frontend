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

package services.P5

import base.SpecBase
import connectors.ArrivalMovementConnector
import generators.Generators
import models.ArrivalMessageType.{ArrivalNotification, UnloadingPermission}
import models.{
  Consignment,
  CustomsOfficeOfDestinationActual,
  IE043Data,
  MessageData,
  MessageMetaData,
  Messages,
  MovementReferenceNumber,
  TraderAtDestination,
  TransitOperation
}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingPermissionMessageServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockConnector = mock[ArrivalMovementConnector]

  private val service = new UnloadingPermissionMessageService(mockConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  private val unloadingPermission1 = MessageMetaData(LocalDateTime.now(), UnloadingPermission, "path/url")
  private val unloadingPermission2 = MessageMetaData(LocalDateTime.now().minusDays(1), UnloadingPermission, "path/url")
  private val unloadingPermission3 = MessageMetaData(LocalDateTime.now().minusDays(2), UnloadingPermission, "path/url")
  private val arrivalNotification  = MessageMetaData(LocalDateTime.now(), ArrivalNotification, "path/url")

  "UnloadingPermissionMessageService" - {

    "getUnloadingPermissionMessage" - {

      "must return latest unloading permission" in {

        val messageMetaData = Messages(List(unloadingPermission1, arrivalNotification, unloadingPermission3, unloadingPermission2))

        when(mockConnector.getMessageMetaData(arrivalId)).thenReturn(Future.successful(messageMetaData))

        service.getUnloadingPermissionMessage(arrivalId).futureValue mustBe Some(unloadingPermission1)
      }

      "must return none when there is no unloading permission" in {

        val messageMetaData = Messages(List(arrivalNotification))

        when(mockConnector.getMessageMetaData(arrivalId)).thenReturn(Future.successful(messageMetaData))

        service.getUnloadingPermissionMessage(arrivalId).futureValue mustBe None
      }
    }

    "getUnloadingPermissionJson" - {

      "must return latest unloading permission message" in {

        val messageMetaData = Messages(List(unloadingPermission1, arrivalNotification, unloadingPermission3, unloadingPermission2))

        val message = IE043Data(
          MessageData(
            preparationDateAndTime = LocalDateTime.now(),
            TransitOperation = TransitOperation(MovementReferenceNumber("23", "GB", "123")),
            Consignment = Consignment(None, None, List.empty),
            CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActual("GB0008"),
            TraderAtDestination = TraderAtDestination("AB123")
          )
        )

        when(mockConnector.getMessageMetaData(arrivalId)).thenReturn(Future.successful(messageMetaData))
        when(mockConnector.getUnloadingPermission(unloadingPermission1.path)).thenReturn(Future.successful(message))

        service.getUnloadingPermission(arrivalId).futureValue mustBe Some(message)
      }

      "must return none when there is no unloading permission message" in {

        val messageMetaData = Messages(List(arrivalNotification))

        when(mockConnector.getMessageMetaData(arrivalId)).thenReturn(Future.successful(messageMetaData))

        service.getUnloadingPermission(arrivalId).futureValue mustBe None
      }
    }
  }
}
