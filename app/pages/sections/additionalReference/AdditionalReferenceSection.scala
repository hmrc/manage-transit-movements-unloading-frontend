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

package pages.sections.additionalReference

import models.Index
import models.reference.AdditionalReferenceType
import pages.QuestionPage
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.sections.additionalReference.AdditionalReferenceSection.AdditionalReference
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, JsPath, Reads}

case class AdditionalReferenceSection(referenceIndex: Index) extends QuestionPage[AdditionalReference] {

  override def path: JsPath = AdditionalReferencesSection.path \ referenceIndex.position

}

object AdditionalReferenceSection {

  case class AdditionalReference(typeValue: AdditionalReferenceType, referenceNumber: Option[String]) {

    override def toString: String = referenceNumber match {
      case Some(refNumber) => s"${typeValue.toString} - $refNumber"
      case None            => typeValue.toString
    }
  }

  object AdditionalReference {

    def reads(index: Index): Reads[AdditionalReference] = (
      (__ \ AdditionalReferenceTypePage(index).toString).read[AdditionalReferenceType] and
        (__ \ AdditionalReferenceNumberPage(index).toString).readNullable[String]
    )(AdditionalReference.apply _)
  }
}
