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
import models.{ArrivalId, CheckMode, Index, NormalMode, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class UnloadingSummaryHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  val seals: (Index, String) => SummaryListRow = {
    (index, value) =>
      SummaryListRow(
        key = messages("changeSeal.sealList.label", index.display).toKey,
        value = Value(value.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.NewSealNumberController.onPageLoad(arrivalId, index, CheckMode).url,
                visuallyHiddenText = Some(messages("changeSeal.sealList.change.hidden", index.display, value)),
                attributes = Map("id" -> s"change-seal-${index.position}")
              )
            )
          )
        )
      )
  }

  val sealsWithRemove: (Index, String) => SummaryListRow = {
    (index, value) =>
      SummaryListRow(
        key = messages("changeSeal.sealList.label", index.display).toKey,
        value = Value(value.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.NewSealNumberController.onPageLoad(arrivalId, index, CheckMode).url,
                visuallyHiddenText = Some(messages("changeSeal.sealList.change.hidden", index.display, value)),
                attributes = Map("id" -> s"change-seal-${index.position}")
              ),
              ActionItem(
                content = messages("site.delete").toText,
                href = routes.ConfirmRemoveSealController.onPageLoad(arrivalId, index, CheckMode).url,
                visuallyHiddenText = Some(messages("changeSeal.sealList.remove.hidden", index.display, value)),
                attributes = Map("id" -> s"remove-seal-${index.position}")
              )
            )
          )
        )
      )
  }

  val items: (Index, String) => SummaryListRow = {
    (index, value) =>
      SummaryListRow(
        key = messages("changeItem.itemList.label", index.display).toKey,
        value = Value(value.toText),
        actions = None
      )
  }

  val vehicleUsed: String => SummaryListRow = {
    value =>
      SummaryListRow(
        key = messages("changeVehicle.reference.label").toKey,
        value = Value(value.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.VehicleNameRegistrationReferenceController.onPageLoad(arrivalId, CheckMode).url,
                visuallyHiddenText = Some(messages("changeVehicle.reference.change.hidden")),
                attributes = Map("id" -> "change-vehicle-reference")
              )
            )
          )
        )
      )
  }

  val registeredCountry: String => SummaryListRow = {
    value =>
      SummaryListRow(
        key = messages("changeVehicle.registeredCountry.label").toKey,
        value = Value(value.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, CheckMode).url,
                visuallyHiddenText = Some(messages("changeVehicle.registeredCountry.change.hidden")),
                attributes = Map("id" -> "change-vehicle-country")
              )
            )
          )
        )
      )
  }

  val grossMass: String => SummaryListRow = {
    value =>
      SummaryListRow(
        key = messages("changeItems.grossMass.label").toKey,
        value = Value(value.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.GrossMassAmountController.onPageLoad(arrivalId, CheckMode).url,
                visuallyHiddenText = Some(messages("changeItems.grossMass.change.hidden")),
                attributes = Map("id" -> "change-gross-mass")
              )
            )
          )
        )
      )
  }

  val totalNumberOfItems: Int => SummaryListRow = {
    value =>
      SummaryListRow(
        key = messages("changeItems.totalNumberOfItems.label").toKey,
        value = Value(value.toString.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.TotalNumberOfItemsController.onPageLoad(arrivalId, CheckMode).url,
                visuallyHiddenText = Some(messages("changeItems.totalNumberOfItems.change.hidden")),
                attributes = Map("id" -> "change-total-number-of-items")
              )
            )
          )
        )
      )
  }

  val totalNumberOfPackages: Int => SummaryListRow = {
    value =>
      SummaryListRow(
        key = messages("changeItems.totalNumberOfPackages.label").toKey,
        value = Value(value.toString.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.TotalNumberOfPackagesController.onPageLoad(arrivalId, CheckMode).url,
                visuallyHiddenText = Some(messages("changeItems.totalNumberOfPackages.change.hidden")),
                attributes = Map("id" -> "change-total-number-of-packages")
              )
            )
          )
        )
      )
  }

  val comments: String => SummaryListRow = {
    value =>
      SummaryListRow(
        key = messages("changeItems.comments.label").toKey,
        value = Value(value.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.ChangesToReportController.onPageLoad(arrivalId, NormalMode).url,
                visuallyHiddenText = Some(messages("changeItems.comments.change.hidden")),
                attributes = Map("id" -> "change-comments")
              ),
              ActionItem(
                content = messages("site.delete").toText,
                href = routes.ConfirmRemoveCommentsController.onPageLoad(arrivalId, NormalMode).url,
                visuallyHiddenText = Some(messages("changeItems.comments.remove.hidden")),
                attributes = Map("id" -> "remove-comments")
              )
            )
          )
        )
      )
  }

  def arrivalId: ArrivalId = userAnswers.id
}
