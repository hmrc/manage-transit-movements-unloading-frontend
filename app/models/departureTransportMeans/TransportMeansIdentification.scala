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

package models.departureTransportMeans

import cats.Order
import models.{DynamicEnumerableType, Radioable}
import org.apache.commons.text.StringEscapeUtils
import play.api.libs.json.{Format, Json}

case class TransportMeansIdentification(`type`: String, description: String) extends Radioable[TransportMeansIdentification] {

  override def toString: String = StringEscapeUtils.unescapeXml(description)

  override val messageKeyPrefix: String = TransportMeansIdentification.messageKeyPrefix

  override val code: String = `type`

}

object TransportMeansIdentification extends DynamicEnumerableType[TransportMeansIdentification] {
  implicit val format: Format[TransportMeansIdentification] = Json.format[TransportMeansIdentification]

  implicit val order: Order[TransportMeansIdentification] = (x: TransportMeansIdentification, y: TransportMeansIdentification) =>
    x.code.compareToIgnoreCase(y.code)

  val messageKeyPrefix = "departureTransportMeans.identification"
}
