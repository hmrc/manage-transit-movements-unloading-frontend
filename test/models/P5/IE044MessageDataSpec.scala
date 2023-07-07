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

package models.P5

import base.SpecBase
import models.MovementReferenceNumber
import models.P5.submission.IE044MessageData
import play.api.libs.json.{JsValue, Json}
import utils.Format.dateTimeFormatIE044

import java.time.LocalDateTime

class IE044MessageDataSpec extends SpecBase {

  "IE044MessageData" - {

    "must serialize" in {

      val prepDateTime = LocalDateTime.now()

      val data = IE044MessageData(
        preparationDateAndTime = prepDateTime,
        TransitOperation = TransitOperation(MovementReferenceNumber("99", "IT", "9876AB88901209")),
        TraderAtDestination = TraderAtDestination("tad-1"),
        CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActual("GB000068")
      )

      val expectedResult: JsValue = Json.parse(s"""
          |{
          |   "messageSender" : "NCTS",
          |   "messageRecipient" : "NTA.GB",
          |   "preparationDateAndTime" : "${prepDateTime.format(dateTimeFormatIE044)}",
          |   "messageIdentification" : "CC044C",
          |   "messageType" : "CC044C",
          |   "@PhaseID" : "NCTS5.0",
          |   "TransitOperation" : {
          |       "MRN" : "99IT9876AB889012096"
          |   },
          |   "TraderAtDestination" : {
          |       "identificationNumber" : "tad-1"
          |   },
          |   "CustomsOfficeOfDestinationActual" : {
          |       "referenceNumber" : "GB000068"
          |   }
          |}
          |""".stripMargin)

      Json.toJson(data) mustBe expectedResult
    }
  }

}
