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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers.running

class SensitiveFormatsSpec extends SpecBase with AppWithDefaultMockFixtures {

  "JsObject" - {
    val encryptedValue = "WFYrOuMf6WHDjHooyzED80QIGXMTPSHEjc3Kl8jPFRJFtHWV"
    val decryptedValue = Json.obj()

    "reads" - {
      "when encryption enabled" - {
        "must read an encrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(encryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result mustEqual decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result mustEqual decryptedValue
          }
        }
      }

      "when encryption disabled" - {
        "must read an encrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(encryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result mustEqual decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result mustEqual decryptedValue
          }
        }
      }
    }

    "writes" - {
      "when encryption enabled" - {
        "must write and encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.jsObjectWrites)
            result must not be decryptedValue
          }
        }
      }

      "encryption disabled" - {
        "must write and not encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.jsObjectWrites)
            result mustEqual decryptedValue
          }
        }
      }
    }
  }

  "CC043CType" - {
    val encryptedValue = encryptedIe043
    val decryptedValue = basicIe043

    "reads" - {
      "when encryption enabled" - {
        "must read an encrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = JsString(encryptedValue).as[CC043CType](sensitiveFormats.cc043cReads)
            result mustEqual decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = JsString(decryptedValue.toXML.toString()).as[CC043CType](sensitiveFormats.cc043cReads)
            result mustEqual decryptedValue
          }
        }
      }

      "when encryption disabled" - {
        "must read an encrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = JsString(encryptedValue).as[CC043CType](sensitiveFormats.cc043cReads)
            result mustEqual decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = JsString(decryptedValue.toXML.toString()).as[CC043CType](sensitiveFormats.cc043cReads)
            result mustEqual decryptedValue
          }
        }
      }
    }

    "writes" - {
      "when encryption enabled" - {
        "must write and encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.cc043cWrites)
            result.as[JsString].value must not startWith "<CC043C>"
          }
        }
      }

      "encryption disabled" - {
        "must write and not encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.cc043cWrites)
            result.as[JsString].value must startWith("<CC043C>")
          }
        }
      }
    }
  }
}
