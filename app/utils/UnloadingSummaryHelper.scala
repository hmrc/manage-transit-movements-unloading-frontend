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
import models.reference.Country
import models.{Index, Mode, Seal, UserAnswers}
import pages._
import play.api.i18n.Messages
import queries.{GoodsItemsQuery, SealsQuery}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class UnloadingSummaryHelper(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  private lazy val (newSeals, existingSeals) = userAnswers
    .get(SealsQuery)
    .map(_.zipWithIndex.partition {
      case (seal, _) => seal.removable
    })
    .getOrElse((Nil, Nil))

  //format: off
  def seals: Seq[SummaryListRow] =
    existingSeals.map {
      case (Seal(sealId, _), position) =>
        val index = Index(position)
        buildRow(
          prefix = "changeSeal.sealList",
          answer = sealId.toText,
          id = Some(s"change-seal-${index.position}"),
          call = Some(controllers.p5.routes.NewSealNumberController.onPageLoad(arrivalId, index, mode)),
          args = index.display, sealId
        )
    }

  def sealsWithRemove: Seq[SummaryListRow] =
    newSeals.map {
      case (Seal(sealId, _), position) =>
        val index = Index(position)
        buildRemovableRow(
          prefix = "changeSeal.sealList",
          answer = sealId.toText,
          id = s"seal-${index.position}",
          changeCall = controllers.p5.routes.NewSealNumberController.onPageLoad(arrivalId, index, mode),
          removeCall = routes.ConfirmRemoveSealController.onPageLoad(arrivalId, index, mode),
          args = index.display, sealId
        )
    }
  //format: on

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
      page = VehicleIdentificationNumberPage,
      formatAnswer = _.toText,
      prefix = "changeVehicle.reference",
      id = Some("change-vehicle-reference"),
      call = Some(controllers.p5.routes.VehicleIdentificationNumberController.onPageLoad(arrivalId, mode))
    )

  def registeredCountry: Option[SummaryListRow] =
    getAnswerAndBuildRow[Country](
      page = VehicleRegistrationCountryPage,
      formatAnswer = _.description.toText,
      prefix = "changeVehicle.registeredCountry",
      id = Some("change-vehicle-country"),
      call = Some(routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, mode))
    )

  def GrossWeight: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = GrossWeightPage,
      formatAnswer = _.toText,
      prefix = "changeItems.GrossWeight",
      id = Some("change-gross-weight"),
      call = Some(controllers.p5.routes.GrossWeightController.onPageLoad(arrivalId, Index(0), mode))
    )

  def totalNumberOfItems: Option[SummaryListRow] =
    getAnswerAndBuildRow[Int](
      page = TotalNumberOfItemsPage,
      formatAnswer = _.toString.toText,
      prefix = "changeItems.totalNumberOfItems",
      id = Some("change-total-number-of-items"),
      call = Some(routes.TotalNumberOfItemsController.onPageLoad(arrivalId, mode))
    )

  def totalNumberOfPackages: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = TotalNumberOfPackagesPage,
      formatAnswer = _.toText,
      prefix = "changeItems.totalNumberOfPackages",
      id = Some("change-total-number-of-packages"),
      call =
        Some(controllers.p5.routes.TotalNumberOfPackagesController.onPageLoad(arrivalId, Index(0), mode)) // TODO add looping to this when we have IE043 data
    )

  def comments: Option[SummaryListRow] =
    getAnswerAndBuildRemovableRow[String](
      page = ChangesToReportPage,
      formatAnswer = _.toText,
      prefix = "changeItems.comments",
      id = "comments",
      changeCall = routes.ChangesToReportController.onPageLoad(arrivalId, mode),
      removeCall = routes.ConfirmRemoveCommentsController.onPageLoad(arrivalId, mode)
    )
}
