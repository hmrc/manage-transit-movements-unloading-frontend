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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptySet
import config.Constants.TransportModeCode._
import connectors.ReferenceDataConnector
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.transport.TransportMode.InlandMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Gen
import pages.equipment.InlandModePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class MeansOfTransportIdentificationTypesServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val mockReferenceDataConnector = mock[ReferenceDataConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector)
      )

  private val meansOfTransportIdentificationTypes = NonEmptySet
    .of(
      "10",
      "11",
      "20",
      "21",
      "30",
      "31",
      "40",
      "41",
      "80",
      "81",
      "99"
    )
    .map(TransportMeansIdentification(_, ""))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataConnector)

    when(mockReferenceDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
      .thenReturn(Future.successful(meansOfTransportIdentificationTypes))
  }

  "getMeansOfTransportIdentificationTypes" - {
    "when inland mode defined" - {
      "and inland mode is Fixed or Unknown" - {
        "must return all identification types except 99" in {
          forAll(Gen.oneOf(Fixed, Unknown), Gen.alphaNumStr) {
            (code, description) =>
              val inlandMode = InlandMode(code, description)

              val service = app.injector.instanceOf[MeansOfTransportIdentificationTypesService]

              val userAnswers = emptyUserAnswers.setValue(InlandModePage, inlandMode)

              val result = service.getMeansOfTransportIdentificationTypes(userAnswers).futureValue

              result.map(_.`type`) mustBe Seq(
                "10",
                "11",
                "20",
                "21",
                "30",
                "31",
                "40",
                "41",
                "80",
                "81"
              )
          }
        }
      }

      "and inland mode is neither Fixed nor Unknown" - {
        "must return identification types starting with the appropriate code" in {
          val inlandMode = InlandMode("3", "")

          val service = app.injector.instanceOf[MeansOfTransportIdentificationTypesService]

          val userAnswers = emptyUserAnswers.setValue(InlandModePage, inlandMode)

          val result = service.getMeansOfTransportIdentificationTypes(userAnswers).futureValue

          result.map(_.`type`) mustBe Seq(
            "30",
            "31"
          )
        }
      }
    }

    "when inland mode undefined" - {
      "must return all identification types except 99" in {
        val service = app.injector.instanceOf[MeansOfTransportIdentificationTypesService]

        val result = service.getMeansOfTransportIdentificationTypes(emptyUserAnswers).futureValue

        result.map(_.`type`) mustBe Seq(
          "10",
          "11",
          "20",
          "21",
          "30",
          "31",
          "40",
          "41",
          "80",
          "81"
        )
      }
    }
  }
}
