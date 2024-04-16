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

package pages.transportEquipment.index

import pages.behaviours.PageBehaviours
import pages.sections.transport.equipment.ItemsSection
import play.api.libs.json.{JsArray, Json}

class ApplyAnItemYesNoPageSpec extends PageBehaviours {

  "ApplyAnItemYesNoPage" - {

    beRetrievable[Boolean](ApplyAnItemYesNoPage(index))

    beSettable[Boolean](ApplyAnItemYesNoPage(index))

    beRemovable[Boolean](ApplyAnItemYesNoPage(index))

    "cleanup must Items Section when no selected" in {
      val userAnswers = emptyUserAnswers
        .setValue(ItemsSection(equipmentIndex), JsArray(Seq(Json.obj("foo" -> "bar"))))

      val result = userAnswers.setValue(ApplyAnItemYesNoPage(equipmentIndex), false)

      result.get(ItemsSection(equipmentIndex)) must not be defined
    }

  }

}
