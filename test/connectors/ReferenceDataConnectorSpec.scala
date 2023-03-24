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
import models.reference.{Country, CustomsOffice}
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
  val code                                           = "GB00001"

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  "Reference Data" - {

    "GET" - {

      "should handle a 200 response for countries" in {
        server.stubFor(
          get(urlEqualTo(countryUri))
            .willReturn(okJson(countryListResponseJson))
        )

        val expectedResult = Seq(
          Country("GB", "United Kingdom"),
          Country("AD", "Andorra")
        )

        connector.getCountries().futureValue mustBe expectedResult
      }

      "should handle client and server errors for countries" in {
        val errorResponseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

        forAll(errorResponseCodes) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(countryUri))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            connector.getCountries().futureValue mustBe Nil
        }
      }

      "should handle a 200 response for customs office with code end point with valid phone number" in {
        server.stubFor(
          get(urlEqualTo(s"$customsOfficeUri/$code"))
            .willReturn(okJson(customsOfficeResponseJsonWithPhone))
        )

        val expectedResult = Some(CustomsOffice("ID1", "NAME001", "GB", Some("004412323232345")))

        connector.getCustomsOffice("GB00001").futureValue mustBe expectedResult
      }

      "should handle a 200 response for customs office with code end point with no phone number" in {
        server.stubFor(
          get(urlEqualTo(s"$customsOfficeUri/$code"))
            .willReturn(okJson(customsOfficeResponseJsonWithOutPhone))
        )

        val expectedResult = Some(CustomsOffice("ID1", "NAME001", "GB", None))

        connector.getCustomsOffice("GB00001").futureValue mustBe expectedResult
      }
      "should handle client and server errors for customs office end point" in {
        val errorResponseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

        forAll(errorResponseCodes) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(s"$customsOfficeUri/$code"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            connector.getCustomsOffice("GB00001").futureValue mustBe None
        }
      }

      "getCountryByCodeV2" - {
        val code             = "GB"
        val countryByCodeUrl = s"/test-only/transit-movements-trader-reference-data/countries/$code"
        "should handle a 200 response" in {

          val countryCodeResponseJson: String =
            """
              | {
              |   "code":"GB",
              |   "state":"valid",
              |   "description":"United Kingdom"
              | }
              |""".stripMargin

          server.stubFor(
            get(urlEqualTo(countryByCodeUrl))
              .willReturn(okJson(countryCodeResponseJson))
          )

          val expectedResult = "United Kingdom"

          connector.getCountryNameByCode(code).futureValue mustBe expectedResult
        }
        "should handle a error response" in {
          val errorResponseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

          forAll(errorResponseCodes) {
            errorResponse =>
              server.stubFor(
                get(urlEqualTo(countryByCodeUrl))
                  .willReturn(
                    aResponse()
                      .withStatus(errorResponse)
                  )
              )

              connector.getCountryNameByCode(code).futureValue mustBe "GB"
          }
        }
      }
    }
  }
}

object ReferenceDataConnectorSpec {

  private val countryUri       = "/test-only/transit-movements-trader-reference-data/countries"
  private val customsOfficeUri = "/test-only/transit-movements-trader-reference-data/customs-office"

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

  private val customsOfficeResponseJsonWithPhone: String =
    """
      | {
      |   "id":"ID1",
      |   "name":"NAME001",
      |   "countryId":"GB",
      |   "phoneNumber":"004412323232345"
      | }
      |""".stripMargin

  private val customsOfficeResponseJsonWithOutPhone: String =
    """
      | {
      |   "id":"ID1",
      |   "name":"NAME001",
      |   "countryId":"GB"
      | }
      |""".stripMargin
}
