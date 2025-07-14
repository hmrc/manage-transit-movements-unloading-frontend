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

package models.P5

import base.SpecBase
import generators.Generators
import models.P5.ArrivalMessageType._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class ArrivalMessageTypeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "ArrivalMessageType" - {
    "must deserialise" - {
      "when a JsString" - {
        "when IE007" in {
          val result = JsString("IE007").as[ArrivalMessageType]
          result mustEqual ArrivalNotification
        }

        "when IE025" in {
          val result = JsString("IE025").as[ArrivalMessageType]
          result mustEqual GoodsReleasedNotification
        }

        "when IE043" in {
          val result = JsString("IE043").as[ArrivalMessageType]
          result mustEqual UnloadingPermission
        }

        "when IE044" in {
          val result = JsString("IE044").as[ArrivalMessageType]
          result mustEqual UnloadingRemarks
        }

        "when IE057" in {
          val result = JsString("IE057").as[ArrivalMessageType]
          result mustEqual RejectionFromOfficeOfDestination
        }

        "when something else" in {
          forAll(nonEmptyString) {
            value =>
              val result = JsString(value).as[ArrivalMessageType]
              result mustEqual Other(value)
          }
        }
      }
    }

    "must fail to deserialise" - {
      "when not a JsString" - {
        val result = Json.obj("foo" -> "bar").validate[ArrivalMessageType]
        result mustBe a[JsError]
      }
    }
  }
}
