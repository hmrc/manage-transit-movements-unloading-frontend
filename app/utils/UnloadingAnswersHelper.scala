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

import models.{Index, UserAnswers}
import pages._
import pages.sections.ItemsSection
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class UnloadingAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends IE043DataHelper(userAnswers) {

  def buildHouseConsignmentRows(
    grossAndNetWeightRows: Seq[SummaryListRow],
    consignorNameRow: Option[SummaryListRow],
    consignorIdentificationRow: Option[SummaryListRow],
    consigneeNameRow: Option[SummaryListRow],
    consigneeIdentificationRow: Option[SummaryListRow]
  ): Seq[SummaryListRow] =
    grossAndNetWeightRows ++ consignorNameRow.map(Seq(_)).getOrElse(Seq.empty) ++ consignorIdentificationRow
      .map(Seq(_))
      .getOrElse(Seq.empty) ++ consigneeNameRow.map(Seq(_)).getOrElse(Seq.empty) ++ consigneeIdentificationRow.map(Seq(_)).getOrElse(Seq.empty)

  def consignorName(houseConsignmentIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConsignorNamePage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consignorName",
    id = None,
    call = None
  )

  def consignorIdentification(houseConsignmentIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConsignorIdentifierPage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consignorIdentifier",
    id = None,
    call = None
  )

  def consigneeName(houseConsignmentIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConsigneeNamePage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consigneeName",
    id = None,
    call = None
  )

  def consigneeIdentification(houseConsignmentIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConsigneeIdentifierPage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consigneeIdentifier",
    id = None,
    call = None
  )

  def houseConsignmentTotalWeightRows(houseConsignmentIndex: Index): Seq[SummaryListRow] = {
    val itemArray      = userAnswers.getIE043(ItemsSection(houseConsignmentIndex))
    val itemCount: Int = itemArray.map(_.value.length).getOrElse(0)

    val itemWeights: Seq[(Option[BigDecimal], Option[BigDecimal])] = itemArray.mapWithIndex[(Option[BigDecimal], Option[BigDecimal])](
      (_, itemIndex) => Some(fetchWeightValues(houseConsignmentIndex, itemIndex))
    )

    if (itemCount == 0) {
      Seq.empty
    } else {
      val totalGrossWeight = itemWeights
        .flatMap(
          x => x._1
        )
        .sum
        .underlying
        .stripTrailingZeros

      val totalNetWeight = itemWeights
        .flatMap(
          x => x._2
        )
        .sum
        .underlying
        .stripTrailingZeros

      val createTotalGrossWeightRow: Seq[SummaryListRow] = totalGrossWeight match {

        case x if x.signum() == 1 => Seq(totalGrossWeightRow(x))
        case _                    => Seq.empty
      }

      val createTotalNetWeightRow: Seq[SummaryListRow] = totalNetWeight match {

        case x if x.signum() == 1 => Seq(totalNetWeightRow(x))
        case _                    => Seq.empty
      }
      createTotalGrossWeightRow ++ createTotalNetWeightRow

    }
  }

  def fetchWeightValues(houseConsignmentIndex: Index, itemIndex: Index): (Option[BigDecimal], Option[BigDecimal]) = {
    val grossWeightDouble = userAnswers.getIE043(GrossWeightPage(houseConsignmentIndex, itemIndex))
    val netWeightDouble   = userAnswers.getIE043(NetWeightPage(houseConsignmentIndex, itemIndex))
    (
      grossWeightDouble.map(BigDecimal.valueOf),
      netWeightDouble.map(BigDecimal.valueOf)
    )
  }

  def totalGrossWeightRow(answer: BigDecimal): SummaryListRow = buildRowWithNoChangeLink(
    answer = formatAsWeight(answer),
    prefix = "unloadingFindings.rowHeadings.houseConsignment.grossWeight",
    args = None
  )

  def totalNetWeightRow(answer: BigDecimal): SummaryListRow = buildRowWithNoChangeLink(
    answer = formatAsWeight(answer),
    prefix = "unloadingFindings.rowHeadings.houseConsignment.netWeight",
    args = None
  )

  def grossWeightRow(houseConsignmentIndex: Index, itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[Double](
    page = GrossWeightPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.grossWeight",
    args = itemIndex.display,
    id = None,
    call = None
  )

  def netWeightRow(houseConsignmentIndex: Index, itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[Double](
    page = NetWeightPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.netWeight",
    args = itemIndex.display,
    id = None,
    call = None
  )
}
