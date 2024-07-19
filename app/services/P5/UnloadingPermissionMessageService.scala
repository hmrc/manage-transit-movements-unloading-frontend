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
import generated._
import models.ArrivalId
import models.P5.ArrivalMessageType._
import models.P5.{ArrivalMessageType, MessageMetaData}
import scalaxb.XMLFormat
import scalaxb.`package`.fromXML
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingPermissionMessageService @Inject() (arrivalMovementConnector: ArrivalMovementConnector) {

  private def getMessageMetaData(
    arrivalId: ArrivalId
  )(
    f: MessageMetaData => Boolean
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[MessageMetaData]] =
    arrivalMovementConnector
      .getMessageMetaData(arrivalId)
      .map(
        _.messages
          .filter(f)
          .sortBy(_.received)
          .reverse
          .headOption
      )

  private def getMessageMetaData(
    arrivalId: ArrivalId,
    messageType: ArrivalMessageType
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[MessageMetaData]] =
    getMessageMetaData(arrivalId) {
      _.messageType == messageType
    }

  def getMessageHead(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[MessageMetaData]] =
    getMessageMetaData(arrivalId) {
      _ => true
    }

  def getIE043(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[CC043CType]] =
    getMessage[CC043CType](arrivalId, UnloadingPermission)

  def getIE044(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[CC044CType]] =
    getMessage[CC044CType](arrivalId, UnloadingRemarks)

  def getMessageId(
    arrivalId: ArrivalId,
    messageType: ArrivalMessageType
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[String]] =
    getMessageMetaData(arrivalId, messageType).map(_.map(_.id))

  private def getMessage[T](
    arrivalId: ArrivalId,
    messageType: ArrivalMessageType
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, format: XMLFormat[T]): Future[Option[T]] =
    (
      for {
        messageMetaData <- OptionT(getMessageMetaData(arrivalId, messageType))
        message         <- OptionT.liftF(arrivalMovementConnector.getMessage(arrivalId, messageMetaData.id).map(fromXML[T](_)))
      } yield message
    ).value
}
