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

package services

import com.google.inject.Singleton

import java.time.{Clock, Instant, LocalDate, LocalDateTime, Year}
import javax.inject.Inject

@Singleton
class DateTimeServiceImpl @Inject() (clock: Clock) extends DateTimeService {

  def currentDateTime: LocalDateTime = LocalDateTime.now(clock)

  def currentDate: LocalDate = LocalDate.now(clock)

  def now: Instant = Instant.now()

  def expiryYear: Int = Year.now(clock).plusYears(4: Long).getValue
}

trait DateTimeService {

  def currentDateTime: LocalDateTime

  def currentDate: LocalDate

  def now: Instant

  def expiryYear: Int
}
