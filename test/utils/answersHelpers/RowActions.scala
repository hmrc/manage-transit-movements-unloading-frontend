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

package utils.answersHelpers

import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions}

trait RowActions {

  def containerIndicatorAction(index: Int): Some[Actions] = Some(
    Actions(
      "",
      List(
        ActionItem(
          "#",
          Text("Change"),
          Some("container identification number"),
          "",
          Map("id" -> s"change-container-identification-number-$index")
        )
      )
    )
  )

  def sealsAction(index: Int): Some[Actions] = Some(
    Actions(
      "",
      List(
        ActionItem(
          "#",
          Text("Change"),
          Some("seal identification number"),
          "",
          Map("id" -> s"change-seal-details-$index")
        )
      )
    )
  )

  val countryAction: Some[Actions] = Some(
    Actions(
      "",
      List(
        ActionItem(
          "#",
          Text("Change"),
          Some("registered country for the departure means of transport"),
          "",
          Map("id" -> "change-registered-country")
        )
      )
    )
  )

  def identificationTypeAction(index: Int): Some[Actions] =
    Some(
      Actions(
        "",
        List(
          ActionItem(
            "#",
            Text("Change"),
            Some("identification type for the departure means of transport"),
            "",
            Map("id" -> s"change-transport-means-identification-$index")
          )
        )
      )
    )

  def identificationNumberAction(index: Int): Some[Actions] =
    Some(
      Actions(
        "",
        List(
          ActionItem(
            "#",
            Text("Change"),
            Some("identification number for the departure means of transport"),
            "",
            Map("id" -> s"change-transport-means-identification-number-$index")
          )
        )
      )
    )

  def additionalReferenceAction(index: Int): Some[Actions] = Some(
    Actions("", List(ActionItem("#", Text("Change"), Some(s"additional reference $index"), "", Map("id" -> s"change-additional-reference-$index"))))
  )

  val grossWeightAction: Some[Actions] = Some(
    Actions(
      "",
      List(
        ActionItem(
          "/manage-transit-movements/unloading/AB123/house-consignment/1/items/1/gross-weight",
          Text("Change"),
          Some("gross weight of item 1"),
          "",
          Map("id" -> "change-gross-weight-1")
        ),
        ActionItem("#", Text("Remove"), Some("gross weight of item 1"), "", Map("id" -> "remove-gross-weight-1"))
      )
    )
  )

  val grossWeightItemAction: Some[Actions] = Some(
    Actions("", List(ActionItem("#", Text("Change"), Some("gross weight of item 1"), "", Map("id" -> "change-gross-weight-1"))))
  )

  val netWeightAction: Some[Actions] = Some(
    Actions("", List(ActionItem("#", Text("Change"), Some("Change Net weight"), "", Map("id" -> "change-net-weight-1"))))
  )

  val netWeightItemAction: Some[Actions] = Some(
    Actions("", List(ActionItem("#", Text("Change"), Some("net weight of item 1"), "", Map("id" -> "change-net-weight-1"))))
  )

  val cusCodeItemAction: Some[Actions] = Some(
    Actions(
      "",
      List(
        ActionItem(
          "/manage-transit-movements/unloading/AB123/house-consignment/1/items/1/cus-code",
          Text("Change"),
          Some("Customs Union and Statistics (CUS) code for item 1"),
          "",
          Map("id" -> "change-cus-code-1")
        )
      )
    )
  )

  val commodityCodeItemAction: Some[Actions] = Some(
    Actions(
      "",
      List(
        ActionItem("#", Text("Change"), Some("commodity code for item 1"), "", Map("id" -> "change-commodity-code-1")),
        ActionItem(
          "/manage-transit-movements/unloading/AB123/house-consignment/1/items/1/commodity-code/remove",
          Text("Remove"),
          Some("commodity code for item 1"),
          "",
          Map("id" -> "remove-commodity-code-1")
        )
      )
    )
  )

  val nomenclatureCodeItemAction: Some[Actions] = Some(
    Actions(
      "",
      List(
        ActionItem(
          "/manage-transit-movements/unloading/AB123/house-consignment/1/items/1/combined-nomenclature-code",
          Text("Change"),
          Some("combined nomenclature code for item 1"),
          "",
          Map("id" -> "change-nomenclature-code-1")
        ),
        ActionItem("#", Text("Remove"), Some("combined nomenclature code for item 1"), "", Map("id" -> "remove-nomenclature-code-1"))
      )
    )
  )
}
