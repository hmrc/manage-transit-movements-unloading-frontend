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

import cats.data.OptionT
import cats.implicits._
import models.{Index, UserAnswers}
import pages._
import pages.sections._
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section

import scala.concurrent.{ExecutionContext, Future}

class HouseConsignmentAnswersHelper(userAnswers: UserAnswers, houseConsignmentIndex: Index, referenceDataService: ReferenceDataService)(implicit
  messages: Messages,
  hc: HeaderCarrier,
  ec: ExecutionContext
) extends AnswersHelper(userAnswers) {

  def buildVehicleNationalityRow(transportMeansIndex: Index): Future[Option[SummaryListRow]] =
    (for {
      x <- OptionT.fromOption[Future](userAnswers.get(DepartureTransportMeansCountryPage(houseConsignmentIndex, transportMeansIndex)))
      y <- OptionT.liftF(referenceDataService.getCountryNameByCode(x))
    } yield transportRegisteredCountry(y)).value

  def buildMeansOfTransportRows(idRow: Option[SummaryListRow], nationalityRow: Option[SummaryListRow]): Seq[SummaryListRow] =
    idRow.map(Seq(_)).getOrElse(Seq.empty) ++
      nationalityRow.map(Seq(_)).getOrElse(Seq.empty)

  def buildTransportSections: Future[Seq[Section]] =
    userAnswers
      .get(DepartureTransportMeansListSection(houseConsignmentIndex))
      .traverse {
        _.zipWithIndex.traverse {
          y =>
            val nationalityRow: Future[Option[SummaryListRow]] = buildVehicleNationalityRow(y._2)
            val meansIdRow: Option[SummaryListRow]             = transportMeansID(y._2)

            nationalityRow.map {
              nationalityRow =>
                Section(
                  messages("unloadingFindings.subsections.transportMeans", y._2.display),
                  buildMeansOfTransportRows(meansIdRow, nationalityRow)
                )
            }
        }
      }
      .map(_.getOrElse(Seq.empty))

  def transportMeansID(transportMeansIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRowWithDynamicPrefix[String](
    answerPath = DepartureTransportMeansIdentificationNumberPage(houseConsignmentIndex, transportMeansIndex),
    titlePath = DepartureTransportMeansIdentificationTypePage(houseConsignmentIndex, transportMeansIndex),
    formatAnswer = formatAsText,
    dynamicPrefix = formatIdentificationTypeAsText,
    id = None,
    call = None
  )

  def transportRegisteredCountry(answer: String): SummaryListRow = buildSimpleRow(
    answer = Text(answer),
    label = messages("unloadingFindings.rowHeadings.vehicleNationality"),
    prefix = "unloadingFindings.rowHeadings.vehicleNationality",
    id = None,
    call = None,
    args = Seq.empty
  )

  def houseConsignmentSection: Seq[Section] = {

    val grossAndNetWeightRows: Option[Seq[SummaryListRow]] = houseConsignmentTotalWeightRows
    val consignorNameRow: Option[SummaryListRow]           = consignorName
    val consignorIdentificationRow: Option[SummaryListRow] = consignorIdentification

    val rows = buildHouseConsignmentRows(grossAndNetWeightRows, consignorNameRow, consignorIdentificationRow)

    Seq(
      Section(
        sectionTitle = messages("unloadingFindings.subsections.houseConsignment", houseConsignmentIndex.display),
        rows
      )
    )
  }

  private def buildHouseConsignmentRows(
    grossAndNetWeightRows: Option[Seq[SummaryListRow]],
    consignorNameRow: Option[SummaryListRow],
    consignorIdentificationRow: Option[SummaryListRow]
  ): Seq[SummaryListRow] =
    grossAndNetWeightRows
      .getOrElse(Seq.empty) ++ consignorNameRow.map(Seq(_)).getOrElse(Seq.empty) ++ consignorIdentificationRow.map(Seq(_)).getOrElse(Seq.empty)

  def consignorName: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConsignorNamePage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consignorName",
    id = None,
    call = None
  )

  def consignorIdentification: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConsignorIdentifierPage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consignorIdentifier",
    id = None,
    call = None
  )

  def houseConsignmentTotalWeightRows: Option[Seq[SummaryListRow]] = {
    val itemArray      = userAnswers.get(ItemsSection(houseConsignmentIndex))
    val itemCount: Int = itemArray.map(_.value.length).getOrElse(0)

    val itemWeights: Seq[(Option[BigDecimal], Option[BigDecimal])] = itemArray.mapWithIndex[(Option[BigDecimal], Option[BigDecimal])](
      (_, itemIndex) => Some(fetchWeightValues(itemIndex))
    )


    if (itemCount == 0) {
      None
    } else {

      val totalGrossWeight: Option[BigDecimal] = itemWeights
        .traverse(
          x => x._1
        )
        .map(_.sum.underlying.stripTrailingZeros)

      val totalNetWeight: Option[BigDecimal] = itemWeights
        .traverse(
          x => x._2
        )
        .map(_.sum.underlying.stripTrailingZeros)

      val createTotalGrossWeightRow: Seq[SummaryListRow] = totalGrossWeight match {

        case Some(x) => Seq(totalGrossWeightRow(x))
        case None    => Seq.empty
      }

      val createTotalNetWeightRow: Seq[SummaryListRow] = totalNetWeight match {

        case Some(x) => Seq(totalNetWeightRow(x))
        case None    => Seq.empty
      }

      Some(createTotalGrossWeightRow ++ createTotalNetWeightRow)

    }
  }

  def fetchWeightValues(itemIndex: Index): (Option[BigDecimal], Option[BigDecimal]) = {
    val grossWeightDouble = userAnswers.get(GrossWeightPage(houseConsignmentIndex, itemIndex))
    val netWeightDouble   = userAnswers.get(NetWeightPage(houseConsignmentIndex, itemIndex))
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

  def itemSections: Seq[Section] =
    userAnswers
      .get(ItemsSection(houseConsignmentIndex))
      .mapWithIndex {
        (_, itemIndex) =>
          val itemDescription: Option[SummaryListRow] = itemDescriptionRow(itemIndex)
          val grossWeight: Option[SummaryListRow]     = grossWeightRow(itemIndex)
          val netWeight: Option[SummaryListRow]       = netWeightRow(itemIndex)

          Some(
            Section(
              messages("unloadingFindings.subsections.item", itemIndex.display),
              Seq(itemDescription, grossWeight, netWeight).flatten
            )
          )
      }

  def itemDescriptionRow(itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ItemDescriptionPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.description",
    id = None,
    call = None
  )

  def grossWeightRow(itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[Double](
    page = GrossWeightPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.grossWeight",
    args = itemIndex.display,
    id = None,
    call = None
  )

  def netWeightRow(itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[Double](
    page = NetWeightPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.netWeight",
    args = itemIndex.display,
    id = None,
    call = None
  )

}
