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
import pages.sections.ItemsSection
import play.api.libs.json.{JsArray, Json}

class AddItemYesNoPageSpec extends PageBehaviours {

  "AddItemYesNoPageSpec" - {

    beRetrievable[Boolean](AddItemYesNoPage(houseConsignmentIndex))

    beSettable[Boolean](AddItemYesNoPage(houseConsignmentIndex))

    beRemovable[Boolean](AddItemYesNoPage(houseConsignmentIndex))
  }

  "cleanup" - {
    "must remove Items section when no selected" in {
      val userAnswers = emptyUserAnswers
        .setValue(AddItemYesNoPage(houseConsignmentIndex), true)
        .setValue(ItemsSection(houseConsignmentIndex), JsArray(Seq(Json.obj("foo" -> "bar"))))

      val result = userAnswers.setValue(AddItemYesNoPage(houseConsignmentIndex), false)

      result.get(ItemsSection(houseConsignmentIndex)) must not be defined

    }

    "must keep Items section when yes selected" in {
      val userAnswers = emptyUserAnswers
        .setValue(AddItemYesNoPage(houseConsignmentIndex), true)
        .setValue(ItemsSection(houseConsignmentIndex), JsArray(Seq(Json.obj("foo" -> "bar"))))

      val result = userAnswers.setValue(AddItemYesNoPage(houseConsignmentIndex), true)

      result.get(ItemsSection(houseConsignmentIndex)) mustBe defined

    }
  }
}
