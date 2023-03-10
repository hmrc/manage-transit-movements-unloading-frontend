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

  def transportEquipmentSectiond: Seq[Section] = {
    val path = JsPath \ "n1:CC043C" \ "Consignment" \ "TransportEquipment"

    userAnswers
      .getIE043[JsArray](path).mapWithIndex {
      (_, index) => {

        val containerRow: Seq[Option[SummaryListRow]] = Seq(containerIdentificationNumber(index))
        val sealRows: Seq[SummaryListRow] = transportEquipmentSeals(index)

        Section(
          messages("unloadingFindings.subsections.transportEquipment", index.display),
          containerRow ++ sealRows
        )
      }
    }
  }

  def transportEquipmentSeals(equipmentIndex: Index): Seq[SummaryListRow] = {

    val path = JsPath \ "n1:CC043C" \ "Consignment" \ "TransportEquipment" \ equipmentIndex.position \ "Seal"

    getAnswersAndBuildSectionRows(path)(sealIndex => transportEquipmentSeal(equipmentIndex, sealIndex))
  }

  def containerIdentificationNumber(index: Index): Option[SummaryListRow] = getAnswerAndBuildRowFromPath[String](
    path = JsPath \ "n1:CC043C" \ "Consignment" \ "TransportEquipment" \ index.position \ "containerIdentificationNumber",
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.containerIdentificationNumber",
    id = Some(s"change-container-identification-number-${index.display}"),
    call = Some(controllers.routes.NewContainerIdentificationNumberController.onPageLoad(arrivalId, index, NormalMode))
  )

  def transportEquipmentSeal(equipmentIndex: Index, sealIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRowFromPath[String](
    path = JsPath \ "n1:CC043C" \ "Consignment" \ "TransportEquipment" \ equipmentIndex.position \ "Seal" \ sealIndex.position,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.sealIdentifier",
    id = Some(s"change-seal-identifier-${sealIndex.display}"),
    call = Some(controllers.routes.NewSealNumberController.onPageLoad(arrivalId, sealIndex, NormalMode)) // TODO add transport equipment to controller / page
  )

}
