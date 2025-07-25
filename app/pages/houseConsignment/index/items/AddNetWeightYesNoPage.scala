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

import models.{Index, UserAnswers}
import pages.QuestionPage
import pages.sections.houseConsignment.index.items.GoodsMeasureSection
import play.api.libs.json.JsPath

import scala.util.Try

case class AddNetWeightYesNoPage(houseConsignmentIndex: Index, itemIndex: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = GoodsMeasureSection(houseConsignmentIndex, itemIndex).path \ toString

  override def toString: String = "addNetWeightYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(false) => userAnswers.remove(NetWeightPage(houseConsignmentIndex, itemIndex))
      case _           => super.cleanup(value, userAnswers)
    }

}
