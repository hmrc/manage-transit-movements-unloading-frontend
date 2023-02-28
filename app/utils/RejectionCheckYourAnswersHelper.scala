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
import models.UserAnswers
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import java.time.LocalDate

class RejectionCheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def vehicleNameRegistration: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = VehicleNameRegistrationReferencePage,
      formatAnswer = _.toText,
      prefix = "vehicleNameRegistrationReference",
      id = Some("change-vehicle-registration-rejection"),
      call = Some(routes.VehicleNameRegistrationRejectionController.onPageLoad(arrivalId))
    )

  def dateGoodsUnloaded: Option[SummaryListRow] =
    getAnswerAndBuildRow[LocalDate](
      page = DateGoodsUnloadedPage,
      formatAnswer = formatAsDate,
      prefix = "dateGoodsUnloaded",
      id = Some("change-date-goods-unloaded"),
      call = Some(routes.DateGoodsUnloadedRejectionController.onPageLoad(arrivalId))
    )

  def totalNumberOfPackages: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = TotalNumberOfPackagesPage,
      formatAnswer = _.toText,
      prefix = "totalNumberOfPackages",
      id = Some("change-total-number-of-packages"),
      call = Some(routes.TotalNumberOfPackagesRejectionController.onPageLoad(arrivalId))
    )

  def totalNumberOfItems: Option[SummaryListRow] =
    getAnswerAndBuildRow[Int](
      page = TotalNumberOfItemsPage,
      formatAnswer = _.toString.toText,
      prefix = "totalNumberOfItems",
      id = Some("change-total-number-of-items"),
      call = Some(routes.TotalNumberOfItemsRejectionController.onPageLoad(arrivalId))
    )

  def grossMassAmount: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = GrossMassAmountPage,
      formatAnswer = _.toText,
      prefix = "grossMassAmount",
      id = Some("change-gross-mass-amount"),
      call = Some(routes.GrossMassAmountRejectionController.onPageLoad(arrivalId))
    )
}
