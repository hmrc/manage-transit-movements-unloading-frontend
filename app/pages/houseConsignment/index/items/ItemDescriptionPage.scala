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

import generated.CommodityType09
import models.Index
import pages.DiscrepancyQuestionPage
import pages.sections.houseConsignment.index.items.CommoditySection
import play.api.libs.json.JsPath

case class ItemDescriptionPage(houseConsignmentIndex: Index, itemIndex: Index) extends DiscrepancyQuestionPage[String, Option[CommodityType09], String] {

  override def path: JsPath = CommoditySection(houseConsignmentIndex, itemIndex).path \ toString

  override def toString: String = "descriptionOfGoods"

  override def valueInIE043(ie043: Option[CommodityType09], sequenceNumber: Option[BigInt]): Option[String] =
    ie043.map(_.descriptionOfGoods)
}
