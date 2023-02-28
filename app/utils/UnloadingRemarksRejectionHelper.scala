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

package utils

import controllers.routes
import models.{FunctionalError, UserAnswers}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import java.time.LocalDate

class UnloadingRemarksRejectionHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def vehicleNameRegistrationReference: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = VehicleNameRegistrationReferencePage,
      formatAnswer = _.toText,
      prefix = "changeVehicle.reference",
      id = Some("change-vehicle-registration-rejection"),
      call = Some(routes.VehicleNameRegistrationRejectionController.onPageLoad(arrivalId))
    )

  def totalNumberOfPackages: Option[SummaryListRow] =
    getAnswerAndBuildRow[Int](
      page = TotalNumberOfPackagesPage,
      formatAnswer = _.toString.toText,
      prefix = "changeItems.totalNumberOfPackages",
      id = Some("change-total-number-of-packages"),
      call = Some(routes.TotalNumberOfPackagesRejectionController.onPageLoad(arrivalId))
    )

  def totalNumberOfItems: Option[SummaryListRow] =
    getAnswerAndBuildRow[Int](
      page = TotalNumberOfItemsPage,
      formatAnswer = _.toString.toText,
      prefix = "changeItems.totalNumberOfItems",
      id = Some("change-total-number-of-items"),
      call = Some(routes.TotalNumberOfItemsRejectionController.onPageLoad(arrivalId))
    )

  def GrossWeightAmount: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = GrossWeightPage,
      formatAnswer = _.toText,
      prefix = "changeItems.GrossWeight",
      id = Some("change-gross-mass-amount"),
      call = Some(routes.GrossWeightAmountRejectionController.onPageLoad(arrivalId))
    )

  def unloadingDate: Option[SummaryListRow] =
    getAnswerAndBuildRow[LocalDate](
      page = DateGoodsUnloadedPage,
      formatAnswer = formatAsDate,
      prefix = "changeItems.dateGoodsUnloaded",
      id = Some("change-date-goods-unloaded"),
      call = Some(routes.DateGoodsUnloadedRejectionController.onPageLoad(arrivalId))
    )
}

object UnloadingRemarksRejectionHelper {

  implicit class RichFunctionalError(functionalError: FunctionalError) {

    def toSummaryList(implicit messages: Messages): SummaryList = SummaryList(
      rows = Seq(
        SummaryListRow(
          key = messages("unloadingRemarksRejection.errorCode").toKey,
          value = Value(functionalError.errorType.toString.toText)
        ),
        SummaryListRow(
          key = messages("unloadingRemarksRejection.pointer").toKey,
          value = Value(functionalError.pointer.value.toText)
        )
      )
    )
  }
}
