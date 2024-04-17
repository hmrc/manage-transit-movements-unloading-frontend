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

package pages.houseConsignment.index.items.document

import models.{Index, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import pages.sections.houseConsignment.index.items.documents.DocumentSection

import scala.util.Try

case class AddAdditionalInformationYesNoPage(houseConsignmentIndex: Index, itemIndex: Index, documentIndex: Index) extends QuestionPage[Boolean, Boolean] {

  override def path: JsPath = DocumentSection(houseConsignmentIndex, itemIndex, documentIndex).path \ toString

  override def toString: String = "addAdditionalInformationYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(false) => userAnswers.remove(AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex))
      case _           => super.cleanup(value, userAnswers)
    }

}
