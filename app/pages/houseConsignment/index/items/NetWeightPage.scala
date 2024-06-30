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

package pages.houseConsignment.index.items

import generated.GoodsMeasureType03
import models.Index
import pages.DiscrepancyQuestionPage
import pages.sections.ItemsSection
import play.api.libs.json.JsPath

case class NetWeightPage(houseConsignment: Index, itemIndex: Index) extends DiscrepancyQuestionPage[BigDecimal, Option[GoodsMeasureType03], BigDecimal] {

  override def path: JsPath = ItemsSection(houseConsignment).path \ itemIndex.position \ "Commodity" \ "GoodsMeasure" \ toString

  override def toString: String = "netMass"

  override def valueInIE043(ie043: Option[GoodsMeasureType03], sequenceNumber: Option[BigInt]): Option[BigDecimal] =
    ie043.flatMap(_.netMass)
}
