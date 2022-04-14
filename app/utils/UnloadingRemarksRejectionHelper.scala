/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import controllers.routes
import models.ArrivalId
import uk.gov.hmrc.viewmodels.MessageInterpolators
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.Text.Literal

import java.time.LocalDate

class UnloadingRemarksRejectionHelper {

  def vehicleNameRegistrationReference(arrivalId: ArrivalId, value: String): Row =
    Row(
      key = Key(msg"changeVehicle.reference.label"),
      value = Value(lit"$value"),
      actions = List(
        Action(
          content = msg"site.edit",
          href = routes.VehicleNameRegistrationRejectionController.onPageLoad(arrivalId).url,
          visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeVehicle.reference.label")),
          attributes = Map("id" -> "change-vehicle-registration-rejection")
        )
      )
    )

  def totalNumberOfPackages(arrivalId: ArrivalId, value: String): Row =
    Row(
      key = Key(msg"changeItems.totalNumberOfPackages.label"),
      value = Value(lit"$value"),
      actions = List(
        Action(
          content = msg"site.edit",
          href = routes.TotalNumberOfPackagesRejectionController.onPageLoad(arrivalId).url,
          visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeItems.totalNumberOfPackages.label"))
        )
      )
    )

  def totalNumberOfItems(arrivalId: ArrivalId, value: String): Row =
    Row(
      key = Key(msg"changeItems.totalNumberOfItems.label"),
      value = Value(lit"$value"),
      actions = List(
        Action(
          content = msg"site.edit",
          href = routes.TotalNumberOfItemsRejectionController.onPageLoad(arrivalId).url,
          visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeItems.totalNumberOfItems.label"))
        )
      )
    )

  def grossMassAmount(arrivalId: ArrivalId, value: String): Row =
    Row(
      key = Key(msg"changeItems.grossMass.label"),
      value = Value(lit"$value"),
      actions = List(
        Action(
          content = msg"site.edit",
          href = routes.GrossMassAmountRejectionController.onPageLoad(arrivalId).url,
          visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeItems.grossMass.label"))
        )
      )
    )

  def unloadingDate(arrivalId: ArrivalId, value: LocalDate): Row =
    Row(
      key = Key(msg"changeItems.dateGoodsUnloaded.label"),
      value = Value(Literal(value.format(Format.cyaDateFormatter))),
      actions = List(
        Action(
          content = msg"site.edit",
          href = routes.DateGoodsUnloadedRejectionController.onPageLoad(arrivalId).url,
          visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeItems.dateGoodsUnloaded.label")),
          attributes = Map("id" -> "change-date-goods-unloaded")
        )
      )
    )
}
