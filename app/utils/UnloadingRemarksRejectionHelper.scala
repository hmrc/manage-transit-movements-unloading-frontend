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

import java.time.LocalDate

import controllers.routes
import models.ArrivalId
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class UnloadingRemarksRejectionHelper(implicit messages: Messages) extends SummaryListRowHelper {

  def vehicleNameRegistrationReference(arrivalId: ArrivalId, value: String): SummaryListRow =
    buildRow(
      prefix = "changeVehicle.reference",
      answer = value.toText,
      id = Some("change-vehicle-registration-rejection"),
      call = Some(routes.VehicleNameRegistrationRejectionController.onPageLoad(arrivalId))
    )

  def totalNumberOfPackages(arrivalId: ArrivalId, value: String): SummaryListRow =
    buildRow(
      prefix = "changeItems.totalNumberOfPackages",
      answer = value.toText,
      id = Some("change-total-number-of-packages"),
      call = Some(routes.TotalNumberOfPackagesRejectionController.onPageLoad(arrivalId))
    )

  def totalNumberOfItems(arrivalId: ArrivalId, value: String): SummaryListRow =
    buildRow(
      prefix = "changeItems.totalNumberOfItems",
      answer = value.toText,
      id = Some("change-total-number-of-items"),
      call = Some(routes.TotalNumberOfItemsRejectionController.onPageLoad(arrivalId))
    )

  def grossMassAmount(arrivalId: ArrivalId, value: String): SummaryListRow =
    buildRow(
      prefix = "changeItems.grossMass",
      answer = value.toText,
      id = Some("change-gross-mass-amount"),
      call = Some(routes.GrossMassAmountRejectionController.onPageLoad(arrivalId))
    )

  def unloadingDate(arrivalId: ArrivalId, value: LocalDate): SummaryListRow =
    buildRow(
      prefix = "changeItems.dateGoodsUnloaded",
      answer = formatAsDate(value),
      id = Some("change-date-goods-unloaded"),
      call = Some(routes.DateGoodsUnloadedRejectionController.onPageLoad(arrivalId))
    )
}
