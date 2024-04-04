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

package pages.departureTransportMeans

import models.reference.TransportMeansIdentification
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours
import pages.departureMeansOfTransport.{AddIdentificationYesNoPage, TransportMeansIdentificationPage}

class AddIdentificationYesNoPageSpec extends PageBehaviours {

  "AddIdentificationYesNoPage" - {

    beRetrievable[Boolean](AddIdentificationYesNoPage(index))

    beSettable[Boolean](AddIdentificationYesNoPage(index))

    beRemovable[Boolean](AddIdentificationYesNoPage(index))

    "cleanup" - {
      "must remove identification page when no selected" in {
        forAll(Arbitrary.arbitrary[TransportMeansIdentification]) {
          transportMeansIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(AddIdentificationYesNoPage(index), true)
              .setValue(TransportMeansIdentificationPage(index), transportMeansIdentification)

            val result = userAnswers.setValue(AddIdentificationYesNoPage(index), false)

            result.get(TransportMeansIdentificationPage(index)) must not be defined
        }
      }

      "must keep identification when yes selected" in {
        forAll(Arbitrary.arbitrary[TransportMeansIdentification]) {
          transportMeansIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(AddIdentificationYesNoPage(index), true)
              .setValue(TransportMeansIdentificationPage(index), transportMeansIdentification)

            val result = userAnswers.setValue(AddIdentificationYesNoPage(index), true)

            result.get(TransportMeansIdentificationPage(index)) mustBe defined
        }
      }
    }
  }
}
