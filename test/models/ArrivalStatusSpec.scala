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

package models

import base.SpecBase
import play.api.libs.json.{JsString, Json}

class ArrivalStatusSpec extends SpecBase {

  "ArrivalStatus" - {

    "must serialise" - {

      "to an UnloadingPermission" in {

        val json = JsString("UnloadingPermission")

        json.validate[ArrivalStatus].asOpt.value mustBe ArrivalStatus.UnloadingPermission
      }

      "to an UnloadingRemarksRejected" in {

        val json = JsString("UnloadingRemarksRejected")

        json.validate[ArrivalStatus].asOpt.value mustBe ArrivalStatus.UnloadingRemarksRejected
      }

      "to a UnloadingRemarksSubmittedNegativeAcknowledgement" in {

        val json = JsString("UnloadingRemarksSubmittedNegativeAcknowledgement")

        json.validate[ArrivalStatus].asOpt.value mustBe ArrivalStatus.UnloadingRemarksSubmittedNegativeAcknowledgement
      }

      "to an OtherStatus when given an unrecognised status" in {

        val json = JsString("Foo")

        json.validate[ArrivalStatus].asOpt.value mustBe ArrivalStatus.OtherStatus
      }
    }

    "must deserialise" in {

      ArrivalStatus.values.map(
        arrivalStatus => Json.toJson(arrivalStatus) mustBe JsString(arrivalStatus.toString)
      )
    }
  }
}
