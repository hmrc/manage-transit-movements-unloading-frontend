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

package pages.houseConsignment.index

import pages.behaviours.PageBehaviours
import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansListSection
import play.api.libs.json.{JsArray, Json}

class AddDepartureTransportMeansYesNoPageSpec extends PageBehaviours {

  "AddCommentsYesNoPage" - {

    beRetrievable[Boolean](AddDepartureTransportMeansYesNoPage(houseConsignmentIndex))

    beSettable[Boolean](AddDepartureTransportMeansYesNoPage(houseConsignmentIndex))

    beRemovable[Boolean](AddDepartureTransportMeansYesNoPage(houseConsignmentIndex))

    "cleanup" - {
      "when no selected" - {
        "must remove departure means of transport section" in {
          val userAnswers = emptyUserAnswers
            .setValue(TransportMeansListSection(houseConsignmentIndex), JsArray(Seq(Json.obj("foo" -> "bar"))))

          val result = userAnswers.setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), false)

          result.get(TransportMeansListSection(houseConsignmentIndex)) must not be defined
        }
      }

      "when yes selected" - {
        "must not remove departure means of transport section" in {
          val userAnswers = emptyUserAnswers
            .setValue(TransportMeansListSection(houseConsignmentIndex), JsArray(Seq(Json.obj("foo" -> "bar"))))

          val result = userAnswers.setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), true)

          result.get(TransportMeansListSection(houseConsignmentIndex)) mustBe defined
        }
      }
    }

  }
}
