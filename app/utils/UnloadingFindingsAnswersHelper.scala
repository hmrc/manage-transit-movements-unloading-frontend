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
import models.{Index, Link, UserAnswers}
import pages._
import pages.sections._
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section

import scala.concurrent.{ExecutionContext, Future}

class UnloadingFindingsAnswersHelper(userAnswers: UserAnswers, referenceDataService: ReferenceDataService)(implicit
  messages: Messages,
  hc: HeaderCarrier,
  ec: ExecutionContext
) extends AnswersHelper(userAnswers) {

  def buildVehicleNationalityRow(index: Index): Future[Option[SummaryListRow]] =
    (for {
      x <- OptionT.fromOption[Future](userAnswers.get(VehicleRegistrationCountryPage(index)))
      y <- OptionT.liftF(referenceDataService.getCountryNameByCode(x))
    } yield transportRegisteredCountry(y)).value

  def buildMeansOfTransportRows(idRow: Option[SummaryListRow], nationalityRow: Option[SummaryListRow]): Seq[SummaryListRow] =
    idRow.map(Seq(_)).getOrElse(Seq.empty) ++
      nationalityRow.map(Seq(_)).getOrElse(Seq.empty)

  def buildTransportSections: Future[Seq[Section]] =
    userAnswers
      .get(TransportMeansListSection)
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
    answerPath = VehicleIdentificationNumberPage(transportMeansIndex),
    titlePath = VehicleIdentificationTypePage(transportMeansIndex),
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

  def transportEquipmentSections: Seq[Section] =
    userAnswers
      .get(TransportEquipmentListSection)
      .mapWithIndex {
        (_, equipmentIndex) =>
          val containerRow: Seq[Option[SummaryListRow]] = Seq(containerIdentificationNumber(equipmentIndex))
          val sealRows: Seq[SummaryListRow]             = transportEquipmentSeals(equipmentIndex)

          val rows = containerRow.head match {
            case Some(containerRow) => Seq(containerRow) ++ sealRows
            case None               => sealRows
          }

          Some(
            Section(
              messages("unloadingFindings.subsections.transportEquipment", equipmentIndex.display),
              rows
            )
          )
      }

  def transportEquipmentSeals(equipmentIndex: Index): Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(SealsSection(equipmentIndex))(
      sealIndex => transportEquipmentSeal(equipmentIndex, sealIndex)
    )

  def containerIdentificationNumber(index: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ContainerIdentificationNumberPage(index),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.containerIdentificationNumber",
    id = None,
    args = None,
    call = None
  )

  def transportEquipmentSeal(equipmentIndex: Index, sealIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = SealPage(equipmentIndex, sealIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.sealIdentifier",
    args = sealIndex.display,
    id = None,
    call = None
  )

  def houseConsignmentSections: Seq[Section] =
    userAnswers.get(HouseConsignmentsSection).mapWithIndex {
      (_, houseConsignmentIndex) =>
        val grossAndNetWeightRows: Option[Seq[SummaryListRow]] = houseConsignmentTotalWeightRows(houseConsignmentIndex)
        val consignorNameRow: Option[SummaryListRow]           = consignorName(houseConsignmentIndex)
        val consignorIdentificationRow: Option[SummaryListRow] = consignorIdentification(houseConsignmentIndex)

        val rows = buildHouseConsignmentRows(grossAndNetWeightRows, consignorNameRow, consignorIdentificationRow)

        Some(
          Section(
            sectionTitle = messages("unloadingFindings.subsections.houseConsignment", houseConsignmentIndex.display),
            rows,
            viewLink = Some(
              Link(
                id = s"view-house-consignment-${houseConsignmentIndex.display}",
                href = controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex).url
              )
            )
          )
        )
    }

  private def buildHouseConsignmentRows(
    grossAndNetWeightRows: Option[Seq[SummaryListRow]],
    consignorNameRow: Option[SummaryListRow],
    consignorIdentificationRow: Option[SummaryListRow]
  ): Seq[SummaryListRow] = (grossAndNetWeightRows, consignorNameRow, consignorIdentificationRow) match {
    case (Some(grossAndNetWeightRows), Some(consignorNameRow), Some(consignorIdentificationRow)) =>
      grossAndNetWeightRows ++ Seq(consignorNameRow, consignorIdentificationRow)
    case (Some(grossAndNetWeightRows), Some(consignorNameRow), None) =>
      grossAndNetWeightRows ++ Seq(consignorNameRow)
    case (Some(grossAndNetWeightRows), None, Some(consignorIdentificationRow)) =>
      grossAndNetWeightRows ++ Seq(consignorIdentificationRow)
    case (None, Some(consignorNameRow), Some(consignorIdentificationRow)) =>
      Seq(consignorNameRow, consignorIdentificationRow)
    case (None, Some(consignorNameRow), None) =>
      Seq(consignorNameRow)
    case (None, None, Some(consignorIdentificationRow)) =>
      Seq(consignorIdentificationRow)
    case (Some(grossAndNetWeightRows), None, None) =>
      grossAndNetWeightRows
    case (_, _, _) =>
      Seq.empty
  }

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

  def houseConsignmentTotalWeightRows(houseConsignmentIndex: Index): Option[Seq[SummaryListRow]] = {
    val itemArray      = userAnswers.get(ItemsSection(houseConsignmentIndex))
    val itemCount: Int = itemArray.map(_.value.length).getOrElse(0)

    val itemWeights: Seq[(Double, Double)] = itemArray.mapWithIndex[(Double, Double)](
      (_, itemIndex) => Some(fetchWeightValues(houseConsignmentIndex, itemIndex))
    )

    if (itemCount == 0) {
      None
    } else {
      val grossWeight = itemWeights
        .map(
          x => x._1
        )
        .sum
      val netWeight = itemWeights
        .map(
          x => x._2
        )
        .sum

      Some(Seq(totalGrossWeightRow(grossWeight), totalNetWeightRow(netWeight)))

    }
  }

  def fetchWeightValues(houseConsignmentIndex: Index, itemIndex: Index): (Double, Double) =
    (
      userAnswers.get(GrossWeightPage(houseConsignmentIndex, itemIndex)).getOrElse(0d),
      userAnswers.get(NetWeightPage(houseConsignmentIndex, itemIndex)).getOrElse(0d)
    )

  def totalGrossWeightRow(answer: Double): SummaryListRow = buildRowWithNoChangeLink(
    answer = formatAsWeight(answer),
    prefix = "unloadingFindings.rowHeadings.houseConsignment.grossWeight",
    args = None
  )

  def totalNetWeightRow(answer: Double): SummaryListRow = buildRowWithNoChangeLink(
    answer = formatAsWeight(answer),
    prefix = "unloadingFindings.rowHeadings.houseConsignment.netWeight",
    args = None
  )

}
