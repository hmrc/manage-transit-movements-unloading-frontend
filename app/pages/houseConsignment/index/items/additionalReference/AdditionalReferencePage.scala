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

package pages.houseConsignment.index.items.additionalReference

import models.Index
import models.reference.AdditionalReferenceType
import pages.QuestionPage
import pages.sections.ItemsSection
import play.api.libs.json.JsPath

case class AdditionalReferencePage(houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index)
    extends QuestionPage[AdditionalReferenceType] {

  override def path: JsPath =
    ItemsSection(houseConsignmentIndex).path \ itemIndex.position \ "AdditionalReference" \ additionalReferenceIndex.position \ toString

  override def toString: String = "type"
}
