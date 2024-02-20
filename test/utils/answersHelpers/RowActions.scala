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
    Actions("", List(ActionItem("#", Text("Change"), Some("Change Gross weight"), "", Map("id" -> "change-gross-weight-1"))))
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

}
