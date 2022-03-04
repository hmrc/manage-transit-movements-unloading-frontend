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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import models.reference.Country
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec
    extends AnyFreeSpec
    with ScalaFutures
    with WireMockSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with IntegrationPatience {

  import ReferenceDataConnectorSpec._

  override protected def portConfigKey: String = "microservice.services.reference-data.port"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Reference Data" - {

    "GET" - {

      "should handle a 200 response" in {
        server.stubFor(
          get(urlEqualTo(uri))
            .willReturn(okJson(countryListResponseJson))
        )

        val app = appBuilder.build()

        running(app) {
          val expectedResult = Seq(
            Country("GB", "United Kingdom"),
            Country("AD", "Andorra")
          )

          val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]
          connector.getCountries.futureValue mustBe expectedResult
        }
      }

      "should handle client and server errors" in {

        val app = appBuilder.build()

        running(app) {
          val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]
          checkErrorResponse(uri, connector.getCountries)
        }
      }
    }
  }

  private def checkErrorResponse(url: String, result: Future[_]): Assertion =
    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        whenReady(result) {
          _ mustBe Nil
        }
    }
}

object ReferenceDataConnectorSpec {

  private val uri = "/transit-movements-trader-reference-data/countries-full-list"

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

  val errorResponses: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)
}
