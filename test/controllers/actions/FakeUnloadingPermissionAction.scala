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

import models.requests.{IdentifierRequest, UnloadingPermissionRequest}
import models.{
  ArrivalId,
  Consignment,
  CustomsOfficeOfDestinationActual,
  EoriNumber,
  MessageData,
  MovementReferenceNumber,
  TraderAtDestination,
  TransitOperation
}
import play.api.mvc.Result
import services.P5.UnloadingPermissionMessageService

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeUnloadingPermissionAction(arrivalId: ArrivalId, unloadingPermissionMessageService: UnloadingPermissionMessageService)
    extends UnloadingPermissionAction(arrivalId, unloadingPermissionMessageService) {

  val messageData: MessageData = MessageData(
    LocalDateTime.now(),
    TransitOperation = TransitOperation(MovementReferenceNumber("99", "IT", "9876AB88901209")),
    TraderAtDestination = TraderAtDestination("identificationNumber"),
    Consignment = Consignment(None, None, List.empty),
    CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActual("referenceNumber")
  )

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, UnloadingPermissionRequest[A]]] =
    Future.successful(Right(UnloadingPermissionRequest(request, EoriNumber("AB123"), messageData)))

}
