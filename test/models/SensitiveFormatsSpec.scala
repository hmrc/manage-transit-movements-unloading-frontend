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
import scalaxb.XMLCalendar

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
            result mustBe decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result mustBe decryptedValue
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
            result mustBe decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result mustBe decryptedValue
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
            result mustBe decryptedValue
          }
        }
      }
    }
  }

  "CC043CType" - {
    val encryptedValue =
      "cmv/7VzwAzxvjPRhffXaotoBxrAgmopT/UoNCOFa6vsGFKEpN11yx/WCRm0u+yJRLEuCgelX9ROrJZFt8XwqIqYoDzJU+b7eQ20JCu2ErmP2kYJh+qD4kRzg1UItlpi7y76iQ4JDfbkY9e1xcQT8VmIeuVj1U261N6jfur6VPZotyRZlpaobdCvBusXAmMwHvl4w2AjV8JwYRzqQ7fR9nmzMUV3jPjwoJIw8hXCOO6BibfB8X17NxAObg4kEzxkFViH3gyu2UVOpmO1SwKD/LMkX/Fs8xp7HeG1GWI/ybyGz+yFwrAn+QETw0aFNw+3xJXSupb21ngXocOvTB3SlpS+ifQqddWR/lqK4G/OYizZFqwVtoost8Geq7GvvFnSFG8qdRSDxGOkpRYXsIZ2ftO4fpAQR64ym2HyxmW08XbIITJRuJ6qL7DKD+y5mytSJ9vZuspnIFscqJf8//BJpYBMxP4amjkBxOPcOEinWVt4vh+h883JgxUxyAr9h/CW9URPdCDYpIEaNtSoEG9op5FD3fM5pEXOu9LHeCNXWN771/CN1jeqc6HMcHWkhOiAnKU+QGIx+Ui7/rzYKkLDmSSap3DE0UXFqWCkXXtLtxkGlWIGkvuYIremm8sD4QXJNuH1IntDTbTtTfROLASJq32EFdcRreUnT8JoE8ardshUO4vyrPLIehg60lHc6EK0IjXS1n7Bu7cnXP/MfMdy1yeOe17EI1z8aXiAm8Ay5yYeJ4DmcBoKbNMrCE7JEIMDc6bfKiBzfq1cTOYiM1XnY6xggBrtdnLM="

    val decryptedValue = CC043CType(
      messageSequence1 = MESSAGESequence(
        messageSender = "",
        messagE_1Sequence2 = MESSAGE_1Sequence(
          messageRecipient = "",
          preparationDateAndTime = XMLCalendar("2022-02-03T08:45:00.000000"),
          messageIdentification = ""
        ),
        messagE_TYPESequence3 = MESSAGE_TYPESequence(
          messageType = CC043C
        ),
        correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
          correlationIdentifier = None
        )
      ),
      TransitOperation = TransitOperationType14(
        MRN = "MRN",
        declarationType = None,
        declarationAcceptanceDate = None,
        security = "0",
        reducedDatasetIndicator = Number0
      ),
      CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActualType03(
        referenceNumber = "cooda"
      ),
      HolderOfTheTransitProcedure = None,
      TraderAtDestination = TraderAtDestinationType03(
        identificationNumber = "tad"
      ),
      CTLControl = None,
      Consignment = None,
      attributes = Map.empty
    )

    "reads" - {
      "when encryption enabled" - {
        "must read an encrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = JsString(encryptedValue).as[CC043CType](sensitiveFormats.cc043cReads)
            result mustBe decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            println("***")
            println(JsString(decryptedValue.toXML.toString()))
            val result = JsString(decryptedValue.toXML.toString()).as[CC043CType](sensitiveFormats.cc043cReads)
            result mustBe decryptedValue
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
            result mustBe decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = JsString(decryptedValue.toXML.toString()).as[CC043CType](sensitiveFormats.cc043cReads)
            result mustBe decryptedValue
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
