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
import models.{CheckMode, UserAnswers}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.Format._

class RejectionCheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def vehicleNameRegistrationRejection: Option[SummaryListRow] = userAnswers.get(VehicleNameRegistrationReferencePage) map {
    answer =>
      SummaryListRow(
        key = messages("vehicleNameRegistrationReference.checkYourAnswersLabel").toKey,
        value = Value(answer.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.VehicleNameRegistrationRejectionController.onPageLoad(userAnswers.id).url,
                visuallyHiddenText = Some(messages("vehicleNameRegistrationReference.change.hidden")),
                attributes = Map("id" -> "change-vehicle-registration-rejection")
              )
            )
          )
        )
      )
  }

  def dateGoodsUnloaded: Option[SummaryListRow] = userAnswers.get(DateGoodsUnloadedPage) map {
    answer =>
      SummaryListRow(
        key = messages("dateGoodsUnloaded.checkYourAnswersLabel").toKey,
        value = Value(answer.format(cyaDateFormatter).toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.DateGoodsUnloadedRejectionController.onPageLoad(userAnswers.id).url,
                visuallyHiddenText = Some(messages("dateGoodsUnloaded.change.hidden")),
                attributes = Map("id" -> "change-date-goods-unloaded")
              )
            )
          )
        )
      )
  }

  def totalNumberOfPackages: Option[SummaryListRow] = userAnswers.get(TotalNumberOfPackagesPage) map {
    answer =>
      SummaryListRow(
        key = messages("totalNumberOfPackages.checkYourAnswersLabel").toKey,
        value = Value(answer.toString.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.TotalNumberOfPackagesRejectionController.onPageLoad(userAnswers.id).url,
                visuallyHiddenText = Some(messages("totalNumberOfPackages.change.hidden")),
                attributes = Map("id" -> "change-total-number-of-packages")
              )
            )
          )
        )
      )
  }

  def totalNumberOfItems: Option[SummaryListRow] = userAnswers.get(TotalNumberOfItemsPage) map {
    answer =>
      SummaryListRow(
        key = messages("totalNumberOfItems.checkYourAnswersLabel").toKey,
        value = Value(answer.toString.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.TotalNumberOfItemsController.onPageLoad(userAnswers.id, CheckMode).url,
                visuallyHiddenText = Some(messages("totalNumberOfItems.change.hidden")),
                attributes = Map("id" -> "change-total-number-of-items")
              )
            )
          )
        )
      )
  }

  def grossMassAmount: Option[SummaryListRow] = userAnswers.get(GrossMassAmountPage) map {
    answer =>
      SummaryListRow(
        key = messages("grossMassAmount.checkYourAnswersLabel").toKey,
        value = Value(answer.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.GrossMassAmountRejectionController.onPageLoad(userAnswers.id).url,
                visuallyHiddenText = Some(messages("grossMassAmount.change.hidden")),
                attributes = Map("id" -> "change-gross-mass-amount")
              )
            )
          )
        )
      )
  }
}
