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

package viewModels.houseConsignment.index.items

import controllers.houseConsignment.index.items.routes
import models.{ArrivalId, Index, Mode}
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.ModeViewModelProvider

import javax.inject.Inject

case class CustomsUnionAndStatisticsCodeViewModel(
  heading: String,
  title: String,
  requiredError: String,
  arrivalId: ArrivalId,
  houseConsignmentMode: Mode,
  itemMode: Mode,
  houseConsignmentIndex: Index,
  itemIndex: Index
) {

  def onSubmit(): Call = routes.CustomsUnionAndStatisticsCodeController.onSubmit(
    arrivalId,
    houseConsignmentMode,
    itemMode,
    houseConsignmentIndex,
    itemIndex
  )
}

object CustomsUnionAndStatisticsCodeViewModel {

  class CustomsUnionAndStatisticsCodeViewModelProvider @Inject() extends ModeViewModelProvider {

    override val prefix = "houseConsignment.item.customsUnionAndStatisticsCode"

    def apply(
      arrivalId: ArrivalId,
      houseConsignmentMode: Mode,
      itemMode: Mode,
      houseConsignmentIndex: Index,
      itemIndex: Index
    )(implicit message: Messages): CustomsUnionAndStatisticsCodeViewModel =
      new CustomsUnionAndStatisticsCodeViewModel(
        heading(itemMode, houseConsignmentIndex, itemIndex),
        title(itemMode, houseConsignmentIndex, itemIndex),
        requiredError(itemMode, houseConsignmentIndex, itemIndex),
        arrivalId,
        houseConsignmentMode,
        itemMode,
        houseConsignmentIndex,
        itemIndex
      )
  }
}
