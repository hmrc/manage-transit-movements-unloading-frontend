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
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import connectors.ReferenceDataConnectorSpec._
import models.reference.{Country, CustomsOffice}
import org.scalacheck.Gen
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockSuite with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.customs-reference-data.port" -> server.port())

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]
  private val code                                           = "GB00001"
  private val countryCode                                    = "GB"

  "Reference Data" - {

    "getCountries" - {
      val url = s"/$baseUrl/lists/CountryCodesFullList"

      "should handle a 200 response for countries" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countryListResponseJson))
        )

        val expectedResult = Seq(
          Country("GB", Some("United Kingdom")),
          Country("AD", Some("Andorra"))
        )

        connector.getCountries().futureValue mustBe expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountries())
      }

      "should handle client and server errors for countries" in {
        checkErrorResponse(url, connector.getCountries())
      }
    }

    "getCustomsOffice" - {
      val url = s"/$baseUrl/filtered-lists/CustomsOffices?data.id=$code"

      "should handle a 200 response for customs office with code end point with valid phone number" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(customsOfficeResponseJsonWithPhone))
        )

        val expectedResult = Seq(CustomsOffice("ID1", "NAME001", "GB", Some("004412323232345")))

        connector.getCustomsOffice(code).futureValue mustBe expectedResult
      }

      "should handle a 200 response for customs office with code end point with no phone number" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(customsOfficeResponseJsonWithOutPhone))
        )

        val expectedResult = Seq(CustomsOffice("ID1", "NAME001", "GB", None))

        connector.getCustomsOffice(code).futureValue mustBe expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCustomsOffice(code))
      }

      "should handle client and server errors for customs office end point" in {
        checkErrorResponse(url, connector.getCustomsOffice(code))
      }
    }

    "getCountryByCode" - {
      val url = s"/$baseUrl/filtered-lists/CountryCodesFullList?data.code=$countryCode"

      "should handle a 200 response" in {

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countryCodeResponseJson))
        )

        val expectedResult = Seq(Country(countryCode, Some("United Kingdom")))

        connector.getCountryNameByCode(countryCode).futureValue mustBe expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountryNameByCode(countryCode))
      }

      "should handle a error response" in {
        checkErrorResponse(url, connector.getCountryNameByCode(countryCode))
      }
    }
  }

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[_]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(emptyResponseJson))
    )

    whenReady[Throwable, Assertion](result.failed) {
      _ mustBe a[NoReferenceDataFoundException]
    }
  }

  private def checkErrorResponse(url: String, result: => Future[_]): Assertion = {
    val errorResponses: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        whenReady[Throwable, Assertion](result.failed) {
          _ mustBe an[Exception]
        }
    }
  }
}

object ReferenceDataConnectorSpec {

  private val countryListResponseJson: String =
    """
      |{
      | "data":
      | [
      |  {
      |    "code":"GB",
      |    "state":"valid",
      |    "description":"United Kingdom"
      |  },
      |  {
      |    "code":"AD",
      |    "state":"valid",
      |    "description":"Andorra"
      |  }
      | ]
      |}
      |""".stripMargin

  private val customsOfficeResponseJsonWithPhone: String =
    """
      |{
      | "data" :
      | [
      |  {
      |    "id":"ID1",
      |    "name":"NAME001",
      |    "countryId":"GB",
      |    "phoneNumber":"004412323232345"
      |  }
      | ]
      |}
      |""".stripMargin

  private val customsOfficeResponseJsonWithOutPhone: String =
    """
      |{
      | "data" :
      | [
      |  {
      |    "id":"ID1",
      |    "name":"NAME001",
      |    "countryId":"GB"
      |  }
      | ]
      |}
      |""".stripMargin

  val countryCodeResponseJson: String =
    """
      |{
      | "data":
      | [
      |  {
      |    "code":"GB",
      |    "state":"valid",
      |    "description":"United Kingdom"
      |  }
      | ]
      |}
      |""".stripMargin

  private val emptyResponseJson: String =
    """
      |{
      |  "data": []
      |}
      |""".stripMargin
}
