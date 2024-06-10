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

package base

import generated._
import scalaxb.XMLCalendar

trait TestMessageData {

  val basicIe043: CC043CType = CC043CType(
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
  val date = "2023-06-09"

  val transitOperation: TransitOperationType14 =
    TransitOperationType14("Mrn", None, Some(XMLCalendar("2023-06-09")), "security", Number0)

  val encryptedIe043 =
    "cmv/7VzwAzxvjPRhffXaotoBxrAgmopT/UoNCOFa6vsGFKEpN11yx/WCRm0u+yJRLEuCgelX9ROrJZFt8XwqIqYoDzJU+b7eQ20JCu2ErmP2kYJh+qD4kRzg1UItlpi7y76iQ4JDfbkY9e1xcQT8VmIeuVj1U261N6jfur6VPZotyRZlpaobdCvBusXAmMwHvl4w2AjV8JwYRzqQ7fR9nmzMUV3jPjwoJIw8hXCOO6BibfB8X17NxAObg4kEzxkFViH3gyu2UVOpmO1SwKD/LMkX/Fs8xp7HeG1GWI/ybyGz+yFwrAn+QETw0aFNw+3xJXSupb21ngXocOvTB3SlpS+ifQqddWR/lqK4G/OYizZFqwVtoost8Geq7GvvFnSFG8qdRSDxGOkpRYXsIZ2ftO4fpAQR64ym2HyxmW08XbIITJRuJ6qL7DKD+y5mytSJ9vZuspnIFscqJf8//BJpYBMxP4amjkBxOPcOEinWVt4vh+h883JgxUxyAr9h/CW9URPdCDYpIEaNtSoEG9op5FD3fM5pEXOu9LHeCNXWN771/CN1jeqc6HMcHWkhOiAnKU+QGIx+Ui7/rzYKkLDmSSap3DE0UXFqWCkXXtLtxkGlWIGkvuYIremm8sD4QXJNuH1IntDTbTtTfROLASJq32EFdcRreUnT8JoE8ardshUO4vyrPLIehg60lHc6EK0IjXS1n7Bu7cnXP/MfMdy1yeOe17EI1z8aXiAm8Ay5yYeJ4DmcBoKbNMrCE7JEIMDc6bfKiBzfq1cTOYiM1XnY6xggBrtdnLM="
}
