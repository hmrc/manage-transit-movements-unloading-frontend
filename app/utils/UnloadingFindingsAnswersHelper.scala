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

import models.{Index, Link, NormalMode, UserAnswers}
import pages.sections.{ItemsSection, NewSealsSection, SealsSection, TransportEquipmentListSection}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section

class UnloadingFindingsAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def departureMeansID: Option[SummaryListRow] = getAnswerAndBuildRowWithDynamicPrefix[String](
    answerPath = VehicleIdentificationNumberPage, // TODO loop with index
    titlePath = VehicleIdentificationTypePage,
    formatAnswer = formatAsText,
    dynamicPrefix = formatIdentificationTypeAsText,
    id = Some("change-departure-means-id"),
    call = Some(controllers.routes.VehicleIdentificationNumberController.onPageLoad(arrivalId, NormalMode))
  )

  def departureRegisteredCountry: Option[SummaryListRow] = getAnswerAndBuildRow[String]( //TODO COUNTRY CODE TO COUNTRY COUNTRY OBJECT
    page = VehicleRegistrationCountryPage,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.vehicleNationality",
    id = Some("change-departure-means-country"),
    call = Some(controllers.routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, NormalMode))
  )

  def transportEquipmentSections: Seq[Section] =
    userAnswers
      .get(TransportEquipmentListSection)
      .mapWithIndex {
        (_, equipmentIndex) =>
          val sealLength = userAnswers
            .get(NewSealsSection(equipmentIndex))
            .map(_.value.length)
            .getOrElse(0)

          val sealPrefixNumber = userAnswers
            .get(SealsSection(equipmentIndex))
            .map(_.value.length)
            .getOrElse(0)

          val containerRow: Seq[Option[SummaryListRow]] = Seq(containerIdentificationNumber(equipmentIndex))
          val sealRows: Seq[SummaryListRow]             = transportEquipmentSeals(equipmentIndex)
          val newSealRows: Seq[SummaryListRow]          = transportEquipmentNewSeals(equipmentIndex, sealPrefixNumber)

          containerRow.head match {
            case Some(containerRow) =>
              Some(
                Section(
                  messages("unloadingFindings.subsections.transportEquipment", equipmentIndex.display),
                  Seq(containerRow) ++ sealRows ++ newSealRows
                )
              )
            case None =>
              Some(
                Section(
                  messages("unloadingFindings.subsections.transportEquipment", equipmentIndex.display),
                  sealRows ++ newSealRows
                )
              )
          }
      }

  def transportEquipmentSeals(equipmentIndex: Index): Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(SealsSection(equipmentIndex))(
      sealIndex => transportEquipmentSeal(equipmentIndex, sealIndex)
    )

  def transportEquipmentNewSeals(equipmentIndex: Index, sealPrefixNumber: Int): Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(NewSealsSection(equipmentIndex))(
      sealIndex => transportEquipmentNewSeal(equipmentIndex, sealIndex, sealPrefixNumber + sealIndex.display)
    )

  def containerIdentificationNumber(index: Index): Option[SummaryListRow] = getAnswerAndBuildRowWithDynamicHiddenText[String](
    page = ContainerIdentificationNumberPage(index),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.containerIdentificationNumber",
    id = Some(s"change-container-identification-number-${index.display}"),
    args = None,
    call = Some(controllers.routes.NewContainerIdentificationNumberController.onPageLoad(arrivalId, index, NormalMode))
  )

  def transportEquipmentSeal(equipmentIndex: Index, sealIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRowWithDynamicHiddenText[String](
    page = SealPage(equipmentIndex, sealIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.sealIdentifier",
    args = Some(Seq(sealIndex.display)),
    id = Some(s"change-seal-identifier-${sealIndex.display}"),
    call = Some(controllers.routes.NewSealNumberController.onPageLoad(arrivalId, equipmentIndex, sealIndex, NormalMode))
  )

  def transportEquipmentNewSeal(equipmentIndex: Index, sealIndex: Index, sealPrefixNumber: Int): Option[SummaryListRow] =
    getAnswerAndBuildRemovableRowWithDynamicHiddenText[String](
      page = NewSealPage(equipmentIndex, sealIndex),
      formatAnswer = formatAsText,
      prefix = "unloadingFindings.rowHeadings.sealIdentifier",
      args = Some(Seq(sealPrefixNumber)),
      id = s"new-seal-identifier-$sealPrefixNumber",
      changeCall = controllers.routes.NewSealNumberController.onPageLoad(arrivalId, equipmentIndex, sealIndex, NormalMode, newSeal = true),
      removeCall = controllers.routes.ConfirmRemoveSealController.onPageLoad(arrivalId, equipmentIndex, sealIndex, NormalMode)
    )

  def itemsSummarySection: Option[Section] = {
    val itemArray      = userAnswers.get(ItemsSection)
    val itemCount: Int = itemArray.map(_.value.length).getOrElse(0)

    val itemWeights: Seq[(Double, Double)] = itemArray.mapWithIndex[(Double, Double)](
      (_, itemIndex) => Some(fetchWeightValues(itemIndex))
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
      Some(
        Section(
          messages("unloadingFindings.subsections.itemSummary"),
          Seq(numberOfItemsRow(itemCount), totalGrossWeightRow(grossWeight), totalNetWeightRow(netWeight))
        )
      )
    }
  }

  def fetchWeightValues(itemIndex: Index): (Double, Double) =
    (userAnswers.get(GrossWeightPage(itemIndex)).getOrElse(0d), userAnswers.get(NetWeightPage(itemIndex)).getOrElse(0d))

  def numberOfItemsRow(answer: Int): SummaryListRow = buildRowWithNoChangeLink(
    answer = formatAsText(answer),
    prefix = "unloadingFindings.rowHeadings.numberOfItems",
    args = None
  )

  def totalGrossWeightRow(answer: Double): SummaryListRow = buildRowWithNoChangeLink(
    answer = formatAsWeight(answer),
    prefix = "unloadingFindings.rowHeadings.totalGrossWeight",
    args = None
  )

  def totalNetWeightRow(answer: Double): SummaryListRow = buildRowWithNoChangeLink(
    answer = formatAsWeight(answer),
    prefix = "unloadingFindings.rowHeadings.totalNetWeight",
    args = None
  )

  def itemSections: Seq[Section] =
    userAnswers
      .get(ItemsSection)
      .mapWithIndex {
        (_, itemIndex) =>
          val itemDescription: Option[SummaryListRow] = itemDescriptionRow(itemIndex)
          val grossWeight: Option[SummaryListRow]     = grossWeightRow(itemIndex)
          val netWeight: Option[SummaryListRow]       = netWeightRow(itemIndex)

          Some(Section(messages("unloadingFindings.subsections.item", itemIndex.display), Seq(itemDescription, grossWeight, netWeight).flatten))
      }

  def itemDescriptionRow(itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ItemDescriptionPage(itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.description",
    id = Some(s"change-item-description-${itemIndex.display}"),
    call = None //TODO: item description change controller
  )

  def grossWeightRow(itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[Double](
    page = GrossWeightPage(itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.grossWeight",
    args = itemIndex.display,
    id = Some(s"change-gross-weight-${itemIndex.display}"),
    call = Some(controllers.routes.GrossWeightController.onPageLoad(arrivalId, itemIndex, NormalMode))
  )

  def netWeightRow(itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[Double](
    page = NetWeightPage(itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.netWeight",
    args = itemIndex.display,
    id = Some(s"change-net-weight-${itemIndex.display}"),
    call = Some(controllers.routes.NetWeightController.onPageLoad(arrivalId, itemIndex, NormalMode))
  )

  def additionalComment: Option[SummaryListRow] = getAnswerAndBuildRemovableRow[String](
    page = UnloadingCommentsPage,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.additionalComments",
    id = "comment",
    changeCall = controllers.routes.UnloadingCommentsController.onPageLoad(arrivalId, NormalMode),
    removeCall = controllers.routes.ConfirmRemoveCommentsController.onPageLoad(arrivalId, NormalMode)
  )

  def addAdditionalComments(): Option[Link] = buildLinkIfAnswerNotPresent(UnloadingCommentsPage) {
    Link(
      id = "add-new-comment",
      text = messages("unloadingFindings.additionalComments.link"),
      href = controllers.routes.UnloadingCommentsController.onPageLoad(arrivalId, NormalMode).url
    )
  }

//  def addNewSeal(equipmentIndex: Index, sealIndex: Index): Option[Link] = buildLink(SealsSection(equipmentIndex)) {
//    Link(
//      id = "view",
//      text = messages("unloadingFindings.addNewSeal.link"),
//      href = controllers.routes.NewSealNumberController.onPageLoad(arrivalId, equipmentIndex, sealIndex, NormalMode, newSeal = true).url
//    )

}
