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

package models.P5.submission

import models.P5._
import play.api.libs.json.{Json, OWrites}
import utils.Format._

import java.time.LocalDateTime

case class IE044MessageData(
  TransitOperation: TransitOperation,
  TraderAtDestination: TraderAtDestination,
  CustomsOfficeOfDestinationActual: CustomsOfficeOfDestinationActual
)

object IE044MessageData {

  implicit val writes: OWrites[IE044MessageData] = OWrites {
    messageData =>
      Json.obj(
        "messageSender"    -> "NCTS", // TODO double check this
        "messageRecipient" -> "NCTS", // TODO double check this
        "preparationDateAndTime" -> LocalDateTime
          .now()
          .format(dateTimeFormatIE044), // TODO this might be an issue, should be on submission this is defined maybe?
        "messageIdentification"            -> "CC044C", // TODO double check this
        "messageType"                      -> "CC044C",
        "@PhaseID"                         -> "NCTS5.0",
        "TransitOperation"                 -> Json.toJsObject(messageData.TransitOperation),
        "TraderAtDestination"              -> Json.toJsObject(messageData.TraderAtDestination),
        "CustomsOfficeOfDestinationActual" -> Json.toJsObject(messageData.CustomsOfficeOfDestinationActual),
        "UnloadingRemark"                  -> Json.obj("unloadingCompletion" -> "1") // TODO this page is missing
      )
  }

}

case class IE044Data(`n1:CC044C`: IE044MessageData)

object IE044Data {

  def fromIE043Data(messageData: MessageData): IE044Data =
    messageData match {
      case messageData =>
        IE044Data(
          IE044MessageData(
            messageData.TransitOperation,
            messageData.TraderAtDestination,
            messageData.CustomsOfficeOfDestinationActual
          )
        )
    }

  implicit val writes: OWrites[IE044Data] = Json.writes[IE044Data]
}
