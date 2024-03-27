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

package pages.houseConsignment.index.items

import pages.behaviours.PageBehaviours
import pages.sections.PackagingListSection
import play.api.libs.json.{JsArray, Json}

class AddPackagesYesNoPageSpec extends PageBehaviours {

  "AddPackagesYesNoPageSpec" - {

    beRetrievable[Boolean](AddPackagesYesNoPage(houseConsignmentIndex, itemIndex))

    beSettable[Boolean](AddPackagesYesNoPage(houseConsignmentIndex, itemIndex))

    beRemovable[Boolean](AddPackagesYesNoPage(houseConsignmentIndex, itemIndex))
  }

  "cleanup" - {
    "must remove packages section when no selected" in {
      val userAnswers = emptyUserAnswers
        .setValue(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), true)
        .setValue(PackagingListSection(houseConsignmentIndex, itemIndex), JsArray(Seq(Json.obj("foo" -> "bar"))))

      val result = userAnswers.setValue(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), false)

      result.get(PackagingListSection(houseConsignmentIndex, itemIndex)) must not be defined

    }

    "must keep packages section when yes selected" in {
      val userAnswers = emptyUserAnswers
        .setValue(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), true)
        .setValue(PackagingListSection(houseConsignmentIndex, itemIndex), JsArray(Seq(Json.obj("foo" -> "bar"))))

      val result = userAnswers.setValue(AddPackagesYesNoPage(houseConsignmentIndex, itemIndex), true)

      result.get(PackagingListSection(houseConsignmentIndex, itemIndex)) mustBe defined

    }
  }
}
