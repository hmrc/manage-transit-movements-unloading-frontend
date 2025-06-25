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
import models.DocType
import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class DocumentType(`type`: DocType, code: String, description: String) extends Selectable {

  override def toString: String = s"${`type`.display} - ($code) $description"

  override val value: String = code
}

object DocumentType {

  def reads(`type`: DocType)(config: FrontendAppConfig): Reads[DocumentType] = {
    val (key, value) = if (config.phase6Enabled) ("key", "value") else ("code", "description")
    (
      (__ \ key).read[String] and
        (__ \ value).read[String]
    ).apply {
      (code, description) =>
        DocumentType(`type`, code, description)
    }
  }

  implicit val format: OFormat[DocumentType] = Json.format[DocumentType]

  implicit val order: Order[DocumentType] = (x: DocumentType, y: DocumentType) => (x, y).compareBy(_.code, _.description, _.`type`.display)
}
