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

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}

sealed case class TransitionError(reason: String)

sealed trait ArrivalStatus

object ArrivalStatus extends Enumerable.Implicits {

  case object UnloadingRemarksSubmittedNegativeAcknowledgement extends ArrivalStatus
  case object UnloadingPermission extends ArrivalStatus
  case object UnloadingRemarksRejected extends ArrivalStatus
  case object OtherStatus extends ArrivalStatus

  val values: Seq[ArrivalStatus] = Seq(
    UnloadingPermission,
    UnloadingRemarksRejected,
    UnloadingRemarksSubmittedNegativeAcknowledgement
  )

  implicit val enumerable: Enumerable[ArrivalStatus] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

  implicit val reads: Reads[ArrivalStatus] =
    Reads {
      case JsString(str) =>
        enumerable
          .withName(str)
          .map {
            s =>
              JsSuccess(s)
          }
          .getOrElse(JsSuccess(OtherStatus))
      case _ =>
        JsError("error.invalid")
    }

}
