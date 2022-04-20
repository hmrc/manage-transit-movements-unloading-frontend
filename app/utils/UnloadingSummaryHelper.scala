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
import models.reference.Country
import models.{CheckMode, Index, NormalMode, UserAnswers}
import pages._
import play.api.i18n.Messages
import queries.{GoodsItemsQuery, SealsQuery}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class UnloadingSummaryHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def seals: Seq[SummaryListRow] =
    userAnswers
      .get(SealsQuery)
      .map(_.zipWithIndex.map {
        case (sealId, position) =>
          val index = Index(position)
          buildRow(
            prefix = "changeItem.sealList",
            answer = sealId.toText,
            id = Some(s"change-seal-${index.position}"),
            call = Some(routes.NewSealNumberController.onPageLoad(arrivalId, index, CheckMode)),
            args = index.display
          )
      })
      .getOrElse(Nil)

  def sealsWithRemove: Seq[SummaryListRow] =
    userAnswers
      .get(SealsQuery)
      .map(_.zipWithIndex.map {
        case (sealId, position) =>
          val index = Index(position)
          buildRemovableRow(
            prefix = "changeItem.sealList",
            answer = sealId.toText,
            id = s"seal-${index.position}",
            changeCall = routes.NewSealNumberController.onPageLoad(arrivalId, index, CheckMode),
            removeCall = routes.ConfirmRemoveSealController.onPageLoad(arrivalId, index, CheckMode),
            args = index.display
          )
      })
      .getOrElse(Nil)

  def items: Seq[SummaryListRow] =
    userAnswers
      .get(GoodsItemsQuery)
      .map(_.zipWithIndex.map {
        case (goodsItemDescription, index) =>
          buildRow(
            prefix = "changeItem.itemList",
            answer = goodsItemDescription.toText,
            id = None,
            call = None,
            args = Index(index).display
          )
      })
      .getOrElse(Nil)

  def vehicleUsed: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = VehicleNameRegistrationReferencePage,
      formatAnswer = _.toText,
      prefix = "changeVehicle.reference",
      id = Some("change-vehicle-reference"),
      call = Some(routes.VehicleNameRegistrationReferenceController.onPageLoad(arrivalId, CheckMode))
    )

  def registeredCountry: Option[SummaryListRow] =
    getAnswerAndBuildRow[Country](
      page = VehicleRegistrationCountryPage,
      formatAnswer = _.description.toText,
      prefix = "changeVehicle.registeredCountry",
      id = Some("change-vehicle-country"),
      call = Some(routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, CheckMode))
    )

  def grossMass: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = GrossMassAmountPage,
      formatAnswer = _.toText,
      prefix = "changeItems.grossMass",
      id = Some("change-gross-mass"),
      call = Some(routes.GrossMassAmountController.onPageLoad(arrivalId, CheckMode))
    )

  def totalNumberOfItems: Option[SummaryListRow] =
    getAnswerAndBuildRow[Int](
      page = TotalNumberOfItemsPage,
      formatAnswer = _.toString.toText,
      prefix = "changeItems.totalNumberOfItems",
      id = Some("change-total-number-of-items"),
      call = Some(routes.TotalNumberOfItemsController.onPageLoad(arrivalId, CheckMode))
    )

  def totalNumberOfPackages: Option[SummaryListRow] =
    getAnswerAndBuildRow[Int](
      page = TotalNumberOfPackagesPage,
      formatAnswer = _.toString.toText,
      prefix = "changeItems.totalNumberOfPackages",
      id = Some("change-total-number-of-packages"),
      call = Some(routes.TotalNumberOfPackagesController.onPageLoad(arrivalId, CheckMode))
    )

  def comments: Option[SummaryListRow] =
    getAnswerAndBuildRemovableRow[String](
      page = ChangesToReportPage,
      formatAnswer = _.toText,
      prefix = "changeItems.comments",
      id = "comments",
      changeCall = routes.ChangesToReportController.onPageLoad(arrivalId, NormalMode),
      removeCall = routes.ConfirmRemoveCommentsController.onPageLoad(arrivalId, NormalMode)
    )
}
