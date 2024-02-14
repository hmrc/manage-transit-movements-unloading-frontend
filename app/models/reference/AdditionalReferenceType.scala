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
import play.api.libs.json.{Format, Json}

case class AdditionalReference(documentType: String, description: String, referenceNumber: Option[String]) {

  override def toString: String = referenceNumber match {
    case Some(refNumber) => s"$documentType - $description - $refNumber"
    case None            => s"$documentType - $description"
  }
}

object AdditionalReference {
  implicit val format: Format[AdditionalReference] = Json.format[AdditionalReference]

}

case class AdditionalReferenceType(documentType: String, description: String)

object AdditionalReferenceType {
  implicit val format: Format[AdditionalReferenceType] = Json.format[AdditionalReferenceType]

  implicit val order: Order[AdditionalReferenceType] = (x: AdditionalReferenceType, y: AdditionalReferenceType) =>
    x.documentType.compareToIgnoreCase(y.documentType)
}