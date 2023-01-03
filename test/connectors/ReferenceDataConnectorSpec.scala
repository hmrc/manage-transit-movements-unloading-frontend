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

package connectors

import base.{AppWithDefaultMockFixtures, SpecBase}
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import connectors.ReferenceDataConnectorSpec._
import models.reference.Country
import org.scalacheck.Gen
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class ReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockSuite {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.reference-data.port" -> server.port())

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  "Reference Data" - {

    "GET" - {

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(uri))
            .willReturn(okJson(countryListResponseJson))
        )

        val expectedResult = Seq(
          Country("GB", "United Kingdom"),
          Country("AD", "Andorra")
        )

        connector.getCountries().futureValue mustBe expectedResult
      }

      "should handle client and server errors" in {
        val errorResponseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

        forAll(errorResponseCodes) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(uri))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            connector.getCountries().futureValue mustBe Nil
        }
      }
    }
  }
}

object ReferenceDataConnectorSpec {

  private val uri = "/transit-movements-trader-reference-data/countries"

  private val countryListResponseJson: String =
    """
      |[
      | {
      |   "code":"GB",
      |   "state":"valid",
      |   "description":"United Kingdom"
      | },
      | {
      |   "code":"AD",
      |   "state":"valid",
      |   "description":"Andorra"
      | }
      |]
      |""".stripMargin
}
