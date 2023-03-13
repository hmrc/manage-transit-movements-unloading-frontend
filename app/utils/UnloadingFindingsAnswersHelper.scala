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

import models.{Index, NormalMode, UserAnswers}
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section

class UnloadingFindingsAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def departureMeansID: Option[SummaryListRow] = getAnswerAndBuildRowFromPathWithDynamicPrefix[String](
    answerPath = JsPath \ "n1:CC043C" \ "Consignment" \ "DepartureTransportMeans" \ 0 \ "identificationNumber", // TODO loop with index
    titlePath = JsPath \ "n1:CC043C" \ "Consignment" \ "DepartureTransportMeans" \ 0 \ "typeOfIdentification",
    formatAnswer = formatAsText,
    dynamicPrefix = formatIdentificationTypeAsText,
    id = Some("change-departure-means-id"),
    call = Some(controllers.routes.VehicleIdentificationNumberController.onPageLoad(arrivalId, NormalMode))
  )

  def departureRegisteredCountry: Option[SummaryListRow] = getAnswerAndBuildRowFromPath[String](
    path = JsPath \ "n1:CC043C" \ "Consignment" \ "DepartureTransportMeans" \ 0 \ "nationality",
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.vehicleNationality",
    id = Some("change-departure-means-country"),
    call = Some(controllers.routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, NormalMode))
  )

  def transportEquipmentSections: Seq[Section] = {
    val transportEquipmentPath = JsPath \ "n1:CC043C" \ "Consignment" \ "TransportEquipment"

    userAnswers
      .getIE043[JsArray](transportEquipmentPath)
      .mapWithIndex {
        (_, equipmentIndex) =>
          val containerRow: Seq[Option[SummaryListRow]] = Seq(containerIdentificationNumber(equipmentIndex))

          val sealRows: Seq[SummaryListRow] = transportEquipmentSeals(equipmentIndex)

          containerRow.head match {
            case Some(containerRow) =>
              Some(Section(messages("unloadingFindings.subsections.transportEquipment", equipmentIndex.display), Seq(containerRow) ++ sealRows))
            case None =>
              Some(Section(messages("unloadingFindings.subsections.transportEquipment", equipmentIndex.display), sealRows))
          }
      }
  }

  def transportEquipmentSeals(equipmentIndex: Index): Seq[SummaryListRow] = {

    val path = JsPath \ "n1:CC043C" \ "Consignment" \ "TransportEquipment" \ equipmentIndex.position \ "Seal"

    getAnswersAndBuildSectionRows(path)(
      sealIndex => transportEquipmentSeal(equipmentIndex, sealIndex)
    )
  }

  def containerIdentificationNumber(index: Index): Option[SummaryListRow] = getAnswerAndBuildRowFromPath[String](
    path = JsPath \ "n1:CC043C" \ "Consignment" \ "TransportEquipment" \ index.position \ "containerIdentificationNumber",
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.containerIdentificationNumber",
    id = Some(s"change-container-identification-number-${index.display}"),
    call = Some(controllers.routes.NewContainerIdentificationNumberController.onPageLoad(arrivalId, index, NormalMode))
  )

  def transportEquipmentSeal(equipmentIndex: Index, sealIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRowFromPath[String](
    path = JsPath \ "n1:CC043C" \ "Consignment" \ "TransportEquipment" \ equipmentIndex.position \ "Seal" \ sealIndex.position \ "identifier",
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.sealIdentifier",
    args = sealIndex.display,
    id = Some(s"change-seal-identifier-${sealIndex.display}"),
    call = Some(controllers.routes.NewSealNumberController.onPageLoad(arrivalId, sealIndex, NormalMode)) // TODO add transport equipment to controller / page
  )

  def itemsSummarySection: Section = {
    val itemsSummaryPath = JsPath \ "n1:CC043C" \ "Consignment" \ "HouseConsignment" \ 0 \ "ConsignmentItem"

    val itemsSummary = userAnswers.getIE043[JsArray](itemsSummaryPath)

    val itemWeights: Seq[(Double, Double)] = itemsSummary.mapWithIndex[(Double, Double)](
      (_, itemIndex) => Some(fetchWeightValues(itemIndex))
    )

    val numberOfItems: Int = itemsSummary.fold(0)(
      x => x.value.size
    )

    if (numberOfItems == 0) {
      Section(None, Seq.empty)
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
      Section(messages("unloadingFindings.subsections.itemSummary"), Seq(numberOfItemsRow(numberOfItems), grossWeightRow(grossWeight), netWeightRow(netWeight)))
    }
  }

  def fetchWeightValues(itemIndex: Index): (Double, Double) = {
    val itemPathGrossWeight =
      JsPath \ "n1:CC043C" \ "Consignment" \ "HouseConsignment" \ 0 \ "ConsignmentItem" \ itemIndex.position \ "Commodity" \ "GoodsMeasure" \ "grossMass"
    val itemPathNetWeight =
      JsPath \ "n1:CC043C" \ "Consignment" \ "HouseConsignment" \ 0 \ "ConsignmentItem" \ itemIndex.position \ "Commodity" \ "GoodsMeasure" \ "netMass"

    (userAnswers.getIE043[Double](itemPathGrossWeight).getOrElse(0d), userAnswers.getIE043[Double](itemPathNetWeight).getOrElse(0d))
  }

  def numberOfItemsRow(answer: Int): SummaryListRow = buildRowWithNoChangeLink(
    answer = formatAsText(answer),
    prefix = "unloadingFindings.rowHeadings.numberOfItems",
    args = None
  )

  def grossWeightRow(answer: Double): SummaryListRow = buildRowWithNoChangeLink(
    answer = formatAsText(answer),
    prefix = "unloadingFindings.rowHeadings.grossWeight",
    args = None
  )

  def netWeightRow(answer: Double): SummaryListRow = buildRowWithNoChangeLink(
    answer = formatAsText(answer),
    prefix = "unloadingFindings.rowHeadings.netWeight",
    args = None
  )

}
