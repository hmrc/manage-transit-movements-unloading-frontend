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

package models

import base.SpecBase

class TransportEquipmentSpec extends SpecBase {

  "TransportEquipment" - {
    "asString" - {
      "when container id is defined" in {
        val transportEquipment = TransportEquipment(Some("123"))
        val result             = transportEquipment.asString
        result mustBe Some("container 123")
      }

      "when container id is undefined" in {
        val transportEquipment = TransportEquipment(None)
        val result             = transportEquipment.asString
        result mustBe None
      }
    }
  }
}
