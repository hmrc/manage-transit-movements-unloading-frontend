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

package pages.sections.houseConsignment.index.items.additionalInformation

import models.Index
import models.reference.AdditionalInformationCode
import pages.QuestionPage
import pages.sections.houseConsignment.index.items.additionalInformation.AdditionalInformationSection.AdditionalInformation
import play.api.libs.json.JsPath

case class AdditionalInformationSection(houseConsignmentIndex: Index, itemIndex: Index, additionalInformationIndex: Index)
    extends QuestionPage[AdditionalInformation] {

  override def path: JsPath = AdditionalInformationsSection(houseConsignmentIndex, itemIndex).path \ additionalInformationIndex.position

}

object AdditionalInformationSection {

  case class AdditionalInformation(typeValue: AdditionalInformationCode, text: Option[String]) {

    override def toString: String = text match {
      case Some(text) => s"${typeValue.toString} - $text"
      case None       => typeValue.toString
    }
  }

}
