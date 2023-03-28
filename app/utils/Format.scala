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

package utils

import logging.Logging
import play.api.libs.json._

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, LocalTime}

object Format {

  val dateFormatter: DateTimeFormatter       = DateTimeFormatter.ofPattern("yyyyMMdd")
  def dateFormatted(date: LocalDate): String = date.format(dateFormatter)

  val timeFormatter: DateTimeFormatter       = DateTimeFormatter.ofPattern("HHmm")
  def timeFormatted(time: LocalTime): String = time.format(timeFormatter)

  val cyaDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  val dateTimeFormatIE044: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

  implicit class RichString(string: String) {
    def parseWithIE044Format: LocalDateTime = LocalDateTime.parse(string, dateTimeFormatIE044)
  }

  val booleanToIntWrites: Writes[Boolean] = (result: Boolean) => {
    val toInt = if (result) "0" else "1"
    JsString(toInt)
  }

  val intToBooleanReads: Reads[Boolean] = (json: JsValue) => {
    json.validate[String].flatMap {
      case "0"   => JsSuccess(true)
      case "1"   => JsSuccess(false)
      case other => JsError(s"Failed to parse $other to boolean")
    }
  }

}

object Date extends Logging {

  def getDate(date: String): Option[LocalDate] =
    try Some(LocalDate.parse(date, Format.dateFormatter))
    catch { case _: Exception => logger.debug("Failed to parse the date"); None }
}

object IntValue extends Logging {

  def getInt(value: String): Option[Int] =
    try Some(value.toInt)
    catch { case _: Exception => logger.debug("failed to get string as Int"); None }
}
