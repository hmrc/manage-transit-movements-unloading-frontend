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
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import forms.mappings.RichSeq

case class CustomsOffice(
  id: String,
  name: String,
  countryId: String,
  phoneNumber: Option[String]
)

object CustomsOffice {

  implicit val writes: OWrites[CustomsOffice] = Json.writes[CustomsOffice]

  implicit val readFromFile: Reads[CustomsOffice] =
    (
      (__ \ "id").read[String] and
        (__ \ "name").read[String] and
        (__ \ "countryId").read[String] and
        (__ \ "phoneNumber").readNullable[String]
    )(CustomsOffice.apply _)

  implicit val order: Order[CustomsOffice] = (x: CustomsOffice, y: CustomsOffice) => {
    (x, y).compareBy(_.name, _.id)
  }

  implicit val listReads: Reads[List[CustomsOffice]] = {

    case class TempCustomsOffice(customsOffice: CustomsOffice, languageCode: String)

    implicit val reads: Reads[TempCustomsOffice] = (
      __.read[CustomsOffice] and
        (__ \ "languageCode").read[String]
    )(TempCustomsOffice.apply _)

    Reads {
      case JsArray(customsOffices) =>
        JsSuccess {
          customsOffices
            .flatMap(_.asOpt[TempCustomsOffice])
            .toSeq
            .groupByPreserveOrder(_.customsOffice.id)
            .flatMap {
              case (_, customsOffices) =>
                customsOffices
                  .find(_.languageCode == "EN")
                  .orElse(customsOffices.headOption)
            }
            .map(_.customsOffice)
            .toList
        }
      case _ => JsError("Expected customs offices to be in a JsArray")
    }
  }
}
