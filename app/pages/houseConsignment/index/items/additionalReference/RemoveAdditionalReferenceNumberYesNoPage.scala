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

import models.{Index, UserAnswers}
import pages.QuestionPage
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferenceSection
import play.api.libs.json.JsPath

import scala.util.Try

case class RemoveAdditionalReferenceNumberYesNoPage(houseConsignmentIndex: Index, itemIndex: Index, additionalReferenceIndex: Index)
    extends QuestionPage[Boolean] {

  override def path: JsPath = AdditionalReferenceSection(houseConsignmentIndex, itemIndex, additionalReferenceIndex).path \ toString

  override def toString: String = "removeAdditionalReferenceNumberYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(false) => userAnswers.remove(AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex))
      case _           => super.cleanup(value, userAnswers)
    }
}
