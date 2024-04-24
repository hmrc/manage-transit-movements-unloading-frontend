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
import models.{DynamicEnumerableType, Radioable}
import org.apache.commons.text.StringEscapeUtils
import play.api.libs.json.{Format, Json}

trait TransportMode[T] extends Radioable[T] {

  val code: String
  val description: String

  override def toString: String = StringEscapeUtils.unescapeXml(description)
}

object TransportMode {

  case class InlandMode(code: String, description: String) extends TransportMode[InlandMode] {
    override val messageKeyPrefix: String = InlandMode.messageKeyPrefix
  }

  object InlandMode extends DynamicEnumerableType[InlandMode] {
    implicit val format: Format[InlandMode] = Json.format[InlandMode]

    implicit val order: Order[InlandMode] = (x: InlandMode, y: InlandMode) => {
      (x, y).compareBy(_.code)
    }

    val messageKeyPrefix = "transport.inlandModeOfTransport"
  }

  case class BorderMode(code: String, description: String) extends TransportMode[BorderMode] {
    override val messageKeyPrefix: String = BorderMode.messageKeyPrefix
  }

  object BorderMode extends DynamicEnumerableType[BorderMode] {
    implicit val format: Format[BorderMode] = Json.format[BorderMode]

    implicit val order: Order[BorderMode] = (x: BorderMode, y: BorderMode) => {
      (x, y).compareBy(_.code)
    }

    val messageKeyPrefix = "transport.border.borderModeOfTransport"
  }
}
