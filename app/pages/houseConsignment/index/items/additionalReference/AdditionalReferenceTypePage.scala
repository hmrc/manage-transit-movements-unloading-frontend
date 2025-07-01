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

import generated.AdditionalReferenceType01
import models.reference.AdditionalReferenceType
import models.{Index, UserAnswers}
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferenceSection
import pages.{DiscrepancyQuestionPage, QuestionPage}
import play.api.libs.json.JsPath

import scala.util.Try

case class AdditionalReferenceTypePage(houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index)
    extends DiscrepancyQuestionPage[AdditionalReferenceType, Seq[AdditionalReferenceType01], String] {

  override def path: JsPath =
    AdditionalReferenceSection(houseConsignmentIndex, itemIndex, additionalReferenceIndex).path \ toString

  override def toString: String = "type"

  override def valueInIE043(ie043: Seq[AdditionalReferenceType01], sequenceNumber: Option[BigInt]): Option[String] =
    ie043
      .find {
        x => sequenceNumber.contains(x.sequenceNumber)
      }
      .map(_.typeValue)
}

case class AdditionalReferenceInCL234Page(houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex).path \ toString

  override def toString: String = "isInCL234"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    lazy val numberPage = AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex)
    lazy val number     = userAnswers.get(numberPage)
    value match {
      case Some(true) if number.contains("0") => userAnswers.remove(numberPage)
      case _                                  => super.cleanup(value, userAnswers)
    }
  }
}
