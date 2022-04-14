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
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class UnloadingRemarksRejectionHelper {

  def vehicleNameRegistrationReference(arrivalId: ArrivalId, value: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = messages("changeVehicle.reference.label").toKey,
      value = Value(value.toText),
      actions = Some(
        Actions(
          "",
          Seq(
            ActionItem(
              content = messages("site.edit").toText,
              href = routes.VehicleNameRegistrationRejectionController.onPageLoad(arrivalId).url,
              visuallyHiddenText = Some(messages("site.edit.hidden", messages("changeVehicle.reference.label"))),
              attributes = Map("id" -> "change-vehicle-registration-rejection")
            )
          )
        )
      )
    )

  def totalNumberOfPackages(arrivalId: ArrivalId, value: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = messages("changeItems.totalNumberOfPackages.label").toKey,
      value = Value(value.toText),
      actions = Some(
        Actions(
          "",
          Seq(
            ActionItem(
              content = messages("site.edit").toText,
              href = routes.TotalNumberOfPackagesRejectionController.onPageLoad(arrivalId).url,
              visuallyHiddenText = Some(messages("site.edit.hidden", messages("changeItems.totalNumberOfPackages.label")))
            )
          )
        )
      )
    )

  def totalNumberOfItems(arrivalId: ArrivalId, value: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = messages("changeItems.totalNumberOfItems.label").toKey,
      value = Value(value.toText),
      actions = Some(
        Actions(
          "",
          Seq(
            ActionItem(
              content = messages("site.edit").toText,
              href = routes.TotalNumberOfItemsRejectionController.onPageLoad(arrivalId).url,
              visuallyHiddenText = Some(messages("site.edit.hidden", messages("changeItems.totalNumberOfItems.label")))
            )
          )
        )
      )
    )

  def grossMassAmount(arrivalId: ArrivalId, value: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = messages("changeItems.grossMass.label").toKey,
      value = Value(value.toText),
      actions = Some(
        Actions(
          "",
          Seq(
            ActionItem(
              content = messages("site.edit").toText,
              href = routes.GrossMassAmountRejectionController.onPageLoad(arrivalId).url,
              visuallyHiddenText = Some(messages("site.edit.hidden", messages("changeItems.grossMass.label")))
            )
          )
        )
      )
    )

  def unloadingDate(arrivalId: ArrivalId, value: LocalDate)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = messages("changeItems.dateGoodsUnloaded.label").toKey,
      value = Value(value.format(Format.cyaDateFormatter).toText),
      actions = Some(
        Actions(
          "",
          Seq(
            ActionItem(
              content = messages("site.edit").toText,
              href = routes.DateGoodsUnloadedRejectionController.onPageLoad(arrivalId).url,
              visuallyHiddenText = Some(messages("site.edit.hidden", messages("changeItems.dateGoodsUnloaded.label"))),
              attributes = Map("id" -> "change-date-goods-unloaded")
            )
          )
        )
      )
    )
}
