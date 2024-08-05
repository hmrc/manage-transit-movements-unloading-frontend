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

package models.P5

import play.api.libs.json.{__, Reads}

trait ArrivalMessageType {
  val value: String

  override def toString: String = value
}

object ArrivalMessageType {

  case object ArrivalNotification extends ArrivalMessageType {
    override val value: String = "IE007"
  }

  case object GoodsReleasedNotification extends ArrivalMessageType {
    override val value: String = "IE025"
  }

  case object UnloadingPermission extends ArrivalMessageType {
    override val value: String = "IE043"
  }

  case object UnloadingRemarks extends ArrivalMessageType {
    override val value: String = "IE044"
  }

  case object RejectionFromOfficeOfDestination extends ArrivalMessageType {
    override val value: String = "IE057"
  }

  case class Other(value: String) extends ArrivalMessageType

  implicit val reads: Reads[ArrivalMessageType] = __.read[String].map {
    case ArrivalNotification.value              => ArrivalNotification
    case GoodsReleasedNotification.value        => GoodsReleasedNotification
    case UnloadingPermission.value              => UnloadingPermission
    case UnloadingRemarks.value                 => UnloadingRemarks
    case RejectionFromOfficeOfDestination.value => RejectionFromOfficeOfDestination
    case value                                  => Other(value)
  }
}
