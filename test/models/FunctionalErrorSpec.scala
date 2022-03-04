/*
 * Copyright 2022 HM Revenue & Customs
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
import com.lucidchart.open.xtract.XmlReader
import generators.MessagesModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class FunctionalErrorSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with MessagesModelGenerators {

  "FunctionalError" - {
    "must deserialize from XML" in {
      forAll(arbitrary[FunctionalError](arbitraryRejectionError)) {
        functionalError =>
          val xml =
            <FUNERRER1>
              <ErrTypER11>{functionalError.errorType.code}</ErrTypER11>
              <ErrPoiER12>{functionalError.pointer.value}</ErrPoiER12>
              {
              functionalError.reason.map {
                reason => <ErrReaER13>{reason}</ErrReaER13>
              } ++
                functionalError.originalAttributeValue.map {
                  originalAttributeValue => <OriAttValER14>{originalAttributeValue}</OriAttValER14>
                }
            }
            </FUNERRER1>

          val result = XmlReader.of[FunctionalError].read(xml).toOption.value

          result mustEqual functionalError
      }
    }
  }
}
