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
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class ManageDocumentsConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockSuite with ScalaCheckPropertyChecks with Generators {

  private lazy val connector: ManageDocumentsConnector = app.injector.instanceOf[ManageDocumentsConnector]
  private val startUrl                                 = "transit-movements-trader-manage-documents"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.manage-documents.port" -> server.port())

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)

  "ManageDocumentsConnectorSpec" - {

    "getUnloadingPermission" - {

      "must return status Ok" in {
        server.stubFor(
          get(urlEqualTo(s"/$startUrl/${arrivalId.value}/unloading-permission-document/$messageId"))
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        val result: Future[WSResponse] = connector.getUnloadingPermission(messageId, arrivalId.value)

        result.futureValue.status mustBe 200
      }

      "must return other error status codes without exceptions" in {

        val genErrorResponse = Gen.oneOf(300, 500).sample.value

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/${arrivalId.value}/unloading-permission-document/$messageId"))
            .willReturn(
              aResponse()
                .withStatus(genErrorResponse)
            )
        )

        val result: Future[WSResponse] = connector.getUnloadingPermission(messageId, arrivalId.value)

        result.futureValue.status mustBe genErrorResponse
      }
    }
  }
}
