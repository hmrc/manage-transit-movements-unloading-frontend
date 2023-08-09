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
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import connectors.ReferenceDataConnectorSpec._
import models.reference.{Country, CustomsOffice}
import org.scalacheck.Gen
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.jdk.CollectionConverters._
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
  val code                                           = "GB00001"
  val countryCode                                    = "GB"

  private val queryParamsCustomsOffice: Seq[(String, StringValuePattern)] = Seq(
    "data.id" -> equalTo(code)
  )

  private val queryParamsCustomsCountry: Seq[(String, StringValuePattern)] = Seq(
    "data.code" -> equalTo(countryCode)
  )

  "Reference Data" - {

    "GET" - {

      "should handle a 200 response for countries" in {
        server.stubFor(
          get(urlEqualTo(s"/$baseUrl/lists/CountryCodesFullList"))
            .willReturn(okJson(countryListResponseJson))
        )

        val expectedResult = Seq(
          Country("GB", Some("United Kingdom")),
          Country("AD", Some("Andorra"))
        )

        connector.getCountries().futureValue mustBe expectedResult
      }

      "should handle client and server errors for countries" in {
        checkErrorResponse(s"/$baseUrl/lists/CountryCodesFullList", connector.getCountries())
      }

      "should handle a 200 response for customs office with code end point with valid phone number" in {
        server.stubFor(
          get(urlPathMatching(s"/$baseUrl/filtered-lists/CustomsOffices"))
            .withQueryParams(queryParamsCustomsOffice.toMap.asJava)
            .willReturn(okJson(customsOfficeResponseJsonWithPhone))
        )

        val expectedResult = Seq(CustomsOffice("ID1", "NAME001", "GB", Some("004412323232345")))

        connector.getCustomsOffice(code).futureValue mustBe expectedResult
      }

      "should handle a 200 response for customs office with code end point with no phone number" in {
        server.stubFor(
          get(urlPathMatching(s"/$baseUrl/filtered-lists/CustomsOffices"))
            .withQueryParams(queryParamsCustomsOffice.toMap.asJava)
            .willReturn(okJson(customsOfficeResponseJsonWithOutPhone))
        )

        val expectedResult = Seq(CustomsOffice("ID1", "NAME001", "GB", None))

        connector.getCustomsOffice(code).futureValue mustBe expectedResult
      }

      "should handle client and server errors for customs office end point" in {
        checkErrorResponse(s"/$baseUrl/filtered-lists/CustomsOffices", connector.getCustomsOffice(code))
      }

      "getCountryByCode" - {
        "should handle a 200 response" in {

          server.stubFor(
            get(urlPathMatching(s"/$baseUrl/filtered-lists/CountryCodesFullList"))
              .withQueryParams(queryParamsCustomsCountry.toMap.asJava)
              .willReturn(okJson(countryCodeResponseJson))
          )

          val expectedResult = Seq(Country(countryCode, Some("United Kingdom")))

          connector.getCountryNameByCode(countryCode).futureValue mustBe expectedResult
        }

        "should handle a error response" in {
          checkErrorResponse(s"/$baseUrl/filtered-lists/CountryCodesFullList", connector.getCountryNameByCode(countryCode))
        }
      }
    }
  }

  private def checkErrorResponse(url: String, result: => Future[_]): Assertion = {
    val errorResponses: Gen[Int] = Gen
      .chooseNum(400: Int, 599: Int)
      .suchThat(_ != 404)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        whenReady(result.failed) {
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
}
