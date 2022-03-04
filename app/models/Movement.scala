/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import play.api.libs.json.{Json, OFormat}

case class Movement(
  movementReferenceNumber: MovementReferenceNumber,
  messages: Seq[MovementMessage]
)

object Movement {
  implicit val formats: OFormat[Movement] = Json.format[Movement]
}

case class MovementMessage(messageType: String, message: String)

object MovementMessage {
  implicit val formats: OFormat[MovementMessage] = Json.format[MovementMessage]
}
