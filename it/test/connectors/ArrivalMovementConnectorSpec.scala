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

import com.github.tomakehurst.wiremock.client.WireMock.*
import itbase.{ItSpecBase, WireMockServerHandler}
import models.MessageStatus
import models.P5.*
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.xml.{Node, NodeSeq}

class ArrivalMovementConnectorSpec extends ItSpecBase with WireMockServerHandler {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.common-transit-convention-traders.port" -> server.port())

  private lazy val connector: ArrivalMovementConnector = app.injector.instanceOf[ArrivalMovementConnector]

  "ArrivalMovementConnectorSpec" - {

    "getMessageMetaData" - {

      val url = s"/movements/arrivals/${arrivalId.value}/messages"

      val expectedResponse =
        """
          |{
          |  "messages": [
          |    {
          |      "_links": {
          |        "self": {
          |          "href": "/customs/transits/movements/arrivals/63498209a2d89ad8/messages/634982098f02f00a"
          |        }
          |      },
          |      "id": "634982098f02f00a",
          |      "received": "2022-11-10T15:32:51.459Z",
          |      "type": "IE007",
          |      "status": "Success"
          |    }
          |  ]
          |}
          |""".stripMargin

      "should return Messages" in {
        server.stubFor(
          get(url)
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.1+json"))
            .willReturn(okJson(expectedResponse))
        )

        val result = connector.getMessageMetaData(arrivalId).futureValue

        val expectedResult = Messages(
          List(
            MessageMetaData(
              LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.ArrivalNotification,
              "634982098f02f00a",
              MessageStatus.Success
            )
          )
        )

        result mustEqual expectedResult
      }
    }

    "getUnloadingPermissionXml" - {

      val xml: Node =
        <ncts:CC043C PhaseID="NCTS5.0" xmlns:ncts="http://ncts.dgtaxud.ec">
          <messageSender>token</messageSender>
        </ncts:CC043C>

      val messageId = "messageId"

      "should return Message" in {
        server.stubFor(
          get(s"/movements/arrivals/${arrivalId.value}/messages/$messageId/body")
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.1+xml"))
            .willReturn(ok(xml.toString()))
        )

        val result = connector.getMessage(arrivalId, messageId).futureValue

        result mustEqual xml
      }
    }

    "submit" - {
      val url = s"/movements/arrivals/${arrivalId.value}/messages"

      val body: NodeSeq =
        <ncts:CC044C PhaseID="NCTS5.0" xmlns:ncts="http://ncts.dgtaxud.ec">
          <messageSender>token</messageSender>
        </ncts:CC044C>

      "must return OK for successful response" in {
        server.stubFor(
          post(urlEqualTo(url))
            .withRequestBody(equalTo(body.toString()))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.1+json"))
            .withHeader("Content-Type", equalTo("application/xml"))
            .willReturn(ok())
        )

        val result = connector.submit(body, arrivalId).futureValue

        result.status mustEqual OK
      }
    }
  }
}
