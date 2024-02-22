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

package pages.sections.additionalInformation

import models.Index
import models.reference.AdditionalInformationCode
import pages.QuestionPage
import pages.additionalInformation.{AdditionalInformationCodePage, AdditionalInformationTextPage}
import pages.sections.additionalInformation.AdditionalInformationSection.AdditionalInformation
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, JsPath, Reads}

case class AdditionalInformationSection(informationIndex: Index) extends QuestionPage[AdditionalInformation] {

  override def path: JsPath = AdditionalInformationListSection.path \ informationIndex.position \ toString

  override def toString: String = "additionalInformation"
}

object AdditionalInformationSection {

  case class AdditionalInformation(code: AdditionalInformationCode, text: Option[String]) {

    override def toString: String = text match {
      case Some(text) => s"${code.toString} - $text"
      case None       => code.toString
    }
  }

  object AdditionalInformation {

    def reads(index: Index): Reads[AdditionalInformation] = (
      (__ \ AdditionalInformationCodePage(index).toString).read[AdditionalInformationCode] and
        (__ \ AdditionalInformationTextPage(index).toString).readNullable[String]
    )(AdditionalInformation.apply _)
  }
}
