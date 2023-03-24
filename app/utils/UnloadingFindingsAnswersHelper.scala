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
import models.reference.Country
import models.{Index, Link, NormalMode, UserAnswers}
import pages.sections._
import pages._
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section
import cats._
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

class UnloadingFindingsAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages, hc: HeaderCarrier, ec: ExecutionContext)
    extends AnswersHelper(userAnswers) {

  def transportMeansSections(referenceDataService: ReferenceDataService): Seq[Nothing] = {
    val x: Seq[Nothing] = userAnswers.get(TransportMeansListSection).mapWithIndex {
      (_, transportMeansIndex) =>
        val transportMeansIDRow = transportMeansID(transportMeansIndex)

        val foo: Future[Option[SummaryListRow]] = userAnswers
          .get(VehicleRegistrationCountryPage(transportMeansIndex))
          .flatTraverse(
            x => referenceDataService.getCountryNameByCode(x).map(transportRegisteredCountry)
          )

        foo.map {
          transportRegisteredCountryRow =>
            val rows = (transportMeansIDRow, transportRegisteredCountryRow) match {
              case (Some(transportMeansIDRow), Some(transportRegisteredCountryRow)) => Seq(transportMeansIDRow, transportRegisteredCountryRow)
              case (Some(transportMeansIDRow), None)                                => Seq(transportMeansIDRow)
              case (None, Some(transportRegisteredCountryRow))                      => Seq(transportRegisteredCountryRow)
              case (_, _)                                                           => Seq.empty
            }

            Some(
              Section(
                sectionTitle = messages("unloadingFindings.subsections.transportMeans", transportMeansIndex.display),
                rows
              )
            )
        }
    }
  }

  def transportMeansID(transportMeansIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRowWithDynamicPrefix[String](
    answerPath = VehicleIdentificationNumberPage(transportMeansIndex),
    titlePath = VehicleIdentificationTypePage(transportMeansIndex),
    formatAnswer = formatAsText,
    dynamicPrefix = formatIdentificationTypeAsText,
    id = None,
    call = None
  )

  def transportRegisteredCountry(answer: String): Option[SummaryListRow] = buildSimpleRow(
    //TODO COUNTRY CODE TO COUNTRY COUNTRY OBJECT
    answer = answer,
    label = "key",
    prefix = "unloadingFindings.rowHeadings.vehicleNationality",
    id = None,
    call = None
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

        val rows = (grossAndNetWeightRows, consignorNameRow, consignorIdentificationRow) match {
          case (Some(grossAndNetWeightRows), Some(consignorNameRow), Some(consignorIdentificationRow)) =>
            grossAndNetWeightRows ++ Seq(consignorNameRow, consignorIdentificationRow)
          case (Some(grossAndNetWeightRows), Some(consignorNameRow), None) =>
            grossAndNetWeightRows ++ Seq(consignorNameRow)
          case (Some(grossAndNetWeightRows), None, Some(consignorIdentificationRow)) =>
            grossAndNetWeightRows ++ Seq(consignorIdentificationRow)
          case (None, Some(consignorNameRow), Some(consignorIdentificationRow)) =>
            Seq(consignorNameRow, consignorIdentificationRow)
          case (_, _, _) =>
            Seq.empty
        }

        Some(
          Section(
            sectionTitle = messages("unloadingFindings.subsections.houseConsignment", houseConsignmentIndex.display),
            rows,
            viewLink = Some(
              Link(
                id = s"view-house-consignment-${houseConsignmentIndex.display}",
                href = controllers.routes.SessionExpiredController.onPageLoad().url
              )
            ) //TODO: Add controller route for specific house consignment
          )
        )

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

  def itemSections(houseConsignmentIndex: Index): Seq[Section] =
    userAnswers
      .get(ItemsSection(houseConsignmentIndex))
      .mapWithIndex {
        (_, itemIndex) =>
          val itemDescription: Option[SummaryListRow] = itemDescriptionRow(houseConsignmentIndex, itemIndex)
          val grossWeight: Option[SummaryListRow]     = grossWeightRow(houseConsignmentIndex, itemIndex)
          val netWeight: Option[SummaryListRow]       = netWeightRow(houseConsignmentIndex, itemIndex)

          Some(
            Section(
              messages("unloadingFindings.subsections.item", itemIndex.display),
              Seq(itemDescription, grossWeight, netWeight).flatten
            )
          )
      }

  def itemDescriptionRow(houseConsignmentIndex: Index, itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ItemDescriptionPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.description",
    id = None,
    call = None //TODO: item description change controller
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
