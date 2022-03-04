/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}

import logging.Logging

object Format {

  val dateFormatter: DateTimeFormatter       = DateTimeFormatter.ofPattern("yyyyMMdd")
  def dateFormatted(date: LocalDate): String = date.format(dateFormatter)

  val timeFormatter: DateTimeFormatter       = DateTimeFormatter.ofPattern("HHmm")
  def timeFormatted(time: LocalTime): String = time.format(timeFormatter)

  val cyaDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
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
