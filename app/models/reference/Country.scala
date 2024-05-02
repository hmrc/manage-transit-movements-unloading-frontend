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

package models.reference

import cats.Order
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

case class Country(code: String, description: String) extends Selectable {
  override def toString: String = s"$description - $code"

  override def toSelectItem(selected: Boolean): SelectItem = SelectItem(Some(code), this.toString, selected)

  override val value: String = code
}

object Country {
  implicit val format: OFormat[Country] = Json.format[Country]

  implicit val order: Order[Country] = (x: Country, y: Country) => {
    (x, y).compareBy(_.description, _.code)
  }

}
