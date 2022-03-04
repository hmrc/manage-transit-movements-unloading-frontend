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

import com.lucidchart.open.xtract.{__, XmlReader}
import play.api.libs.json.{JsString, Writes}

sealed abstract class ErrorPointer(val value: String)

object ErrorPointer extends Serializable {

  implicit val xmlReader: XmlReader[ErrorPointer] =
    __.read[String].map {
      pointer =>
        values
          .find(_.value.equalsIgnoreCase(pointer))
          .getOrElse(DefaultPointer(pointer))
    }

  implicit val writes: Writes[ErrorPointer] = Writes[ErrorPointer] {
    pointer: ErrorPointer =>
      JsString(pointer.value)
  }

  val values = Seq(
    GrossMassPointer,
    NumberOfItemsPointer,
    UnloadingDatePointer,
    VehicleRegistrationPointer,
    NumberOfPackagesPointer
  )

}

object GrossMassPointer extends ErrorPointer("HEA.Total gross mass")

object NumberOfItemsPointer extends ErrorPointer("HEA.Total number of items")

object UnloadingDatePointer extends ErrorPointer("REM.Unloading Date")

object VehicleRegistrationPointer extends ErrorPointer("HEA.Identity of means of transport at departure (exp/trans)")

object NumberOfPackagesPointer extends ErrorPointer("HEA.Total number of packages")

case class DefaultPointer(override val value: String) extends ErrorPointer(value)
