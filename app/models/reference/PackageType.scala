/*
 * Copyright 2024 HM Revenue & Customs
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
import config.FrontendAppConfig
import play.api.libs.functional.syntax.*
import play.api.libs.json.{__, Json, OFormat, Reads}

case class PackageType(code: String, description: String) extends Selectable {

  override def toString: String = s"($code) $description"

  override val value: String = code
}

object PackageType {

  def reads(config: FrontendAppConfig): Reads[PackageType] =
    if (config.phase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(PackageType.apply)
    } else {
      Json.reads[PackageType]
    }

  implicit val format: OFormat[PackageType] = Json.format[PackageType]

  implicit val order: Order[PackageType] = (x: PackageType, y: PackageType) => (x, y).compareBy(_.description, _.code)

  def queryParams(code: String)(config: FrontendAppConfig): Seq[(String, String)] = {
    val key = if (config.phase6Enabled) "keys" else "data.code"
    Seq(key -> code)
  }
}
