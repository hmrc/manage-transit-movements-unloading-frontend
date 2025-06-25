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
import config.FrontendAppConfig
import models.{DynamicEnumerableType, Radioable}
import play.api.libs.functional.syntax.*
import play.api.libs.json.{__, Format, Json, Reads}

case class QualifierOfIdentification(qualifier: String, description: String) extends Radioable[QualifierOfIdentification] {
  override def toString: String         = description
  override val messageKeyPrefix: String = "qualifierOfIdentification"
  override val code: String             = qualifier
}

object QualifierOfIdentification extends DynamicEnumerableType[QualifierOfIdentification] {

  def reads(config: FrontendAppConfig): Reads[QualifierOfIdentification] =
    if (config.phase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(QualifierOfIdentification.apply)
    } else {
      Json.reads[QualifierOfIdentification]
    }

  implicit val format: Format[QualifierOfIdentification] = Json.format[QualifierOfIdentification]

  implicit val order: Order[QualifierOfIdentification] = (x: QualifierOfIdentification, y: QualifierOfIdentification) => (x, y).compareBy(_.qualifier)
}
