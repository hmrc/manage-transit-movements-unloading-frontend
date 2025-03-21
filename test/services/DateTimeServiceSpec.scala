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

import base.{AppWithDefaultMockFixtures, SpecBase}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{Clock, Duration, Instant, ZoneId}

class DateTimeServiceSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks {

  "DateTimeService" - {
    "currentDateTime" - {

      "must return different times for different system clocks" in {

        forAll(Gen.choose(-12, 12)) {
          hour =>
            val firstZoneId = "UTC"
            val secondZoneId = firstZoneId + (hour match {
              case _ if hour < 0 => s"$hour"
              case _             => s"+$hour"
            })

            val clock1: Clock = Clock.system(ZoneId.of(firstZoneId))
            val clock2: Clock = Clock.system(ZoneId.of(secondZoneId))

            val dataTimeService1 = new DateTimeServiceImpl(clock1)
            val dataTimeService2 = new DateTimeServiceImpl(clock2)

            val duration = Duration.between(dataTimeService1.currentDateTime, dataTimeService2.currentDateTime)

            duration.toHours mustEqual hour
        }

      }
    }

    "expiryYear" - {
      "must return the correct year" in {
        val fixedInstant = Instant.parse("2023-12-03T10:15:30.00Z")

        val clock: Clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

        val dataTimeService = new DateTimeServiceImpl(clock)

        val result = dataTimeService.expiryYear

        result mustEqual 2027
      }
    }
  }
}
