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

import models.Index
import pages.QuestionPage
import pages.sections.ItemSection
import play.api.libs.json.JsPath

case class ItemDescriptionPage(houseConsignmentIndex: Index, itemIndex: Index) extends QuestionPage[String] {

  override def path: JsPath = ItemSection(houseConsignmentIndex, itemIndex).path \ "Commodity" \ toString

  override def toString: String = "descriptionOfGoods"
}

// TODO - you will have some json that looks like
/*
 "ConsignmentItem" : [
   {
     "declarationGoodsItemNumber" : 1, <- this needs to be set in the ConsignmentItemTransformer
     "Commodity" : {
        "descriptionOfGoods" : "foo" <- this gets set in the CommodityTransformer
     }
   }
 ]

 some kind of find within the array to find the object that has the declaration goods item number that you're after. We're then after the description inside that object.
 */
