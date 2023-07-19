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

package models.submission

import models.MessageData
import play.api.libs.json.{Json, OWrites}

import java.time.LocalDateTime

case class IE044Data(`n1:CC044C`: IE044MessageData)

object IE044Data {

  def fromIE043Data(messageData: MessageData, preparationDateAndTime: LocalDateTime = LocalDateTime.now()): IE044Data =
    messageData match {
      case messageData =>
        IE044Data(
          IE044MessageData(
            preparationDateAndTime = preparationDateAndTime,
            TransitOperation = messageData.TransitOperation,
            TraderAtDestination = messageData.TraderAtDestination,
            CustomsOfficeOfDestinationActual = messageData.CustomsOfficeOfDestinationActual
          )
        )
    }

  implicit val writes: OWrites[IE044Data] = Json.writes[IE044Data]
}
