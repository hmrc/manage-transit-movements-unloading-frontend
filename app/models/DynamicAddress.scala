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

package models

import generated.{AddressType14, AddressType15}
import play.api.libs.json.{Json, OFormat}

case class DynamicAddress(
  numberAndStreet: String,
  city: String,
  postalCode: Option[String]
) {

  override def toString: String = Seq(Some(numberAndStreet), Some(city), postalCode).flatten.mkString("<br>")
}

object DynamicAddress {
  implicit val format: OFormat[DynamicAddress] = Json.format[DynamicAddress]

  def apply(address: AddressType14): DynamicAddress =
    new DynamicAddress(address.streetAndNumber, address.city, address.postcode)

  def apply(address: AddressType15): DynamicAddress =
    new DynamicAddress(address.streetAndNumber, address.city, address.postcode)
}
