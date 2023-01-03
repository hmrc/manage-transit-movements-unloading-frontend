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
import generators.Generators
import models.messages.{InterchangeControlReference, Meta}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDateTime

class MetaServiceSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val mockDateTimeService = mock[DateTimeService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDateTimeService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DateTimeService].toInstance(mockDateTimeService))

  "MetaServiceSpec" - {

    "return a Meta model" in {
      val metaService = app.injector.instanceOf[MetaService]

      forAll(arbitrary[InterchangeControlReference]) {
        interchangeControlReference =>
          val localDateTime = LocalDateTime.now()

          when(mockDateTimeService.currentDateTime).thenReturn(localDateTime)

          metaService.build(interchangeControlReference) mustBe Meta(
            interchangeControlReference,
            localDateTime.toLocalDate,
            localDateTime.toLocalTime
          )
      }
    }
  }
}
