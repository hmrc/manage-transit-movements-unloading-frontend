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

package pages.houseConsignment.index.documents

import generated.SupportingDocumentType02
import models.Index
import pages.DiscrepancyQuestionPage
import pages.sections.houseConsignment.index.documents.DocumentSection
import play.api.libs.json.JsPath

case class AdditionalInformationPage(houseConsignmentIndex: Index, documentIndex: Index)
    extends DiscrepancyQuestionPage[String, Seq[SupportingDocumentType02], String] {

  override def path: JsPath = DocumentSection(houseConsignmentIndex, documentIndex).path \ toString

  override def toString: String = "additionalInformation"

  override def valueInIE043(ie043: Seq[SupportingDocumentType02], sequenceNumber: Option[BigInt]): Option[String] =
    ie043
      .find {
        x => sequenceNumber.contains(BigInt(x.sequenceNumber))
      }
      .flatMap(_.complementOfInformation)
}
