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

package pages.houseConsignment.index.items

import pages.behaviours.PageBehaviours
import pages.sections.houseConsignment.index.items.documents.DocumentsSection
import play.api.libs.json.{JsArray, Json}

class AddDocumentYesNoPageSpec extends PageBehaviours {

  "AddDocumentYesNoPage" - {

    beRetrievable[Boolean](AddDocumentYesNoPage(houseConsignmentIndex, itemIndex))

    beSettable[Boolean](AddDocumentYesNoPage(houseConsignmentIndex, itemIndex))

    beRemovable[Boolean](AddDocumentYesNoPage(houseConsignmentIndex, itemIndex))
  }

  "cleanup" - {
    "must remove all documents" in {
      val userAnswers = emptyUserAnswers
        .setValue(DocumentsSection(houseConsignmentIndex, itemIndex), JsArray(Seq(Json.obj("foo" -> "bar"))))

      val result = userAnswers.setValue(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), false)

      result.get(DocumentsSection(houseConsignmentIndex, itemIndex)) must not be defined
    }

    "must keep documents when yes selected" in {
      val userAnswers = emptyUserAnswers
        .setValue(DocumentsSection(houseConsignmentIndex, itemIndex), JsArray(Seq(Json.obj("foo" -> "bar"))))

      val result = userAnswers.setValue(AddDocumentYesNoPage(houseConsignmentIndex, itemIndex), true)

      result.get(DocumentsSection(houseConsignmentIndex, itemIndex)) mustBe defined
    }
  }
}
