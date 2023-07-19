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

import play.api.libs.json.{JsString, Json, Reads, Writes}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class DateGoodsUnloaded(date: LocalDate)

object DateGoodsUnloaded {

  implicit val writes: Writes[DateGoodsUnloaded] = (dateGoodsUnloaded: DateGoodsUnloaded) => {

    val formatterNoMillis: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    JsString(dateGoodsUnloaded.date.atStartOfDay().format(formatterNoMillis))
  }

  implicit val reads: Reads[DateGoodsUnloaded] = Json.reads[DateGoodsUnloaded]

}
