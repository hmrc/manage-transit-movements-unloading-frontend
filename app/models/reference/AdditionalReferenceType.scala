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
import play.api.libs.json.{__, Format, Json, Reads}

case class AdditionalReferenceType(documentType: String, description: String) extends Selectable {

  override def toString: String = s"$documentType - $description"

  override val value: String = documentType
}

object AdditionalReferenceType {

  def reads(config: FrontendAppConfig): Reads[AdditionalReferenceType] =
    if (config.phase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(AdditionalReferenceType.apply)
    } else {
      Json.reads[AdditionalReferenceType]
    }

  implicit val format: Format[AdditionalReferenceType] = Json.format[AdditionalReferenceType]

  implicit val order: Order[AdditionalReferenceType] = (x: AdditionalReferenceType, y: AdditionalReferenceType) =>
    (x, y).compareBy(_.description, _.documentType)

}
