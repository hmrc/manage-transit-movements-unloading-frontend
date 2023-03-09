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

import cats.data.OptionT
import connectors.ArrivalMovementConnector
import models.ArrivalId
import models.P5.ArrivalMessageType.UnloadingPermission
import models.P5.{Message, MessageMetaData}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingPermissionMessageService @Inject() (arrivalMovementConnector: ArrivalMovementConnector) {

  def getUnloadingPermissionMessage(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[MessageMetaData]] =
    arrivalMovementConnector
      .getMessageMetaData(arrivalId)
      .map(
        _.messages
          .filter(_.messageType == UnloadingPermission)
          .sortBy(_.received)
          .reverse
          .headOption
      )

  def getUnloadingPermissionJson(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[Message]] =
    (
      for {
        unloadingPermissionMessage <- OptionT(getUnloadingPermissionMessage(arrivalId))
        unloadingPermission        <- OptionT.liftF(arrivalMovementConnector.getMessage(unloadingPermissionMessage.path))
      } yield unloadingPermission
    ).value

}
