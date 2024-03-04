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
import models.reference.AdditionalInformationType
import pages.QuestionPage
import pages.houseConsignment.index.items.additionalinformation.{HouseConsignmentAdditionalInformationCodePage, HouseConsignmentAdditionalInformationTextPage}
import pages.sections.houseConsignment.index.items.additionalInformation.AdditionalInformationSection.AdditionalInformation
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, JsPath, Reads}

case class AdditionalInformationSection(houseConsignmentIndex: Index, itemIndex: Index, additionalInformationIndex: Index)
    extends QuestionPage[AdditionalInformation] {

  override def path: JsPath = AdditionalInformationsSection(houseConsignmentIndex, itemIndex).path \ additionalInformationIndex.position

}

object AdditionalInformationSection {

  case class AdditionalInformation(typeValue: AdditionalInformationType, text: Option[String]) {

    override def toString: String = text match {
      case Some(text) => s"${typeValue.toString} - $text"
      case None       => typeValue.toString
    }
  }

  object AdditionalInformation {

    def reads(houseConsignmentIndex: Index, itemIndex: Index, additionalInformationIndex: Index): Reads[AdditionalInformation] = (
      (__ \ HouseConsignmentAdditionalInformationCodePage(houseConsignmentIndex, itemIndex, additionalInformationIndex).toString)
        .read[AdditionalInformationType] and
        (__ \ HouseConsignmentAdditionalInformationTextPage(houseConsignmentIndex, itemIndex, additionalInformationIndex).toString).readNullable[String]
    )(AdditionalInformation.apply _)
  }
}
