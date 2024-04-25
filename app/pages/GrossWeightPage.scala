/*
 * Copyright 2023 HM Revenue & Customs
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

package pages

import generated.GoodsMeasureType03
import pages.sections.OtherQuestionsSection
import play.api.libs.json.JsPath

case object GrossWeightPage extends DiscrepancyQuestionPage[BigDecimal, Option[GoodsMeasureType03], BigDecimal] {

  override def path: JsPath = OtherQuestionsSection.path \ toString

  override def toString: String = "grossMass"

  override def valueInIE043(ie043: Option[GoodsMeasureType03], sequenceNumber: Option[BigInt]): Option[BigDecimal] =
    ie043.map(_.grossMass)
}
