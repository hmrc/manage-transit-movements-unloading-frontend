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

package pages.sections.houseConsignment.index.items.dangerousGoods

import models.Index
import pages.sections.Section
import pages.sections.houseConsignment.index.items.CommoditySection
import play.api.libs.json.{JsArray, JsPath}

case class DangerousGoodsListSection(houseConsignmentIndex: Index, itemIndex: Index) extends Section[JsArray] {

  override def path: JsPath = CommoditySection(houseConsignmentIndex, itemIndex).path \ toString

  override def toString: String = "DangerousGoods"
}
