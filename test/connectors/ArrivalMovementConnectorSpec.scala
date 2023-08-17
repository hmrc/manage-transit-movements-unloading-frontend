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
import models.MovementReferenceNumber
import models.P5._
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global

class ArrivalMovementConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockSuite with Generators {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.common-transit-convention-traders.port" -> server.port())

  private lazy val connector: ArrivalMovementConnector = app.injector.instanceOf[ArrivalMovementConnector]

  "ArrivalMovementConnectorSpec" - {

    "getUnloadingPermission" - {

      val expectedResponse =
        """
          |{
          |"_links": {
          |        "self": {
          |          "href": "/customs/transits/movements/arrivals/63498209a2d89ad8/messages/634982098f02f00a"
          |   }
          |},
          |      "received": "2022-11-10T15:32:51.459Z",
          |      "type": "IE007",
          |  "body": {
          |   "n1:CC043C": {
          |     "preparationDateAndTime": "2022-11-10T15:32:51.459Z",
          |     "TransitOperation": {
          |       "MRN": "99IT9876AB88901209"
          |     },
          |     "Consignment": {
          |       "HouseConsignment": []
          |     },
          |     "TraderAtDestination": {
          |       "identificationNumber": "AB123"
          |     },
          |     "CustomsOfficeOfDestinationActual": {
          |       "referenceNumber": "GB0008"
          |     }
          |   }
          | }
          |}
          |""".stripMargin

      "should return Message" in {
        val url = s"/movements/arrivals/${arrivalId.value}/messages/$messageId"

        server.stubFor(
          get(url)
            .withHeader("Accept", containing("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(expectedResponse))
        )

        val result = connector.getUnloadingPermission(arrivalId, messageId).futureValue

        val expectedResult = IE043Data(
          MessageData(
            preparationDateAndTime = LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
            TransitOperation = TransitOperation(MovementReferenceNumber("99IT9876AB88901209").get),
            Consignment = Consignment(None, None, List.empty),
            CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActual("GB0008"),
            TraderAtDestination = TraderAtDestination("AB123")
          )
        )

        result mustBe expectedResult
      }
    }

  }

}
