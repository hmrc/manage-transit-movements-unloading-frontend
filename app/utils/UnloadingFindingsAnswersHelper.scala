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

import models.departureTransportMeans.TransportMeansIdentification
import models.reference.Country
import models.{Index, Link, UserAnswers}
import pages._
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import pages.sections._
import pages.sections.additionalReference.AdditionalReferenceSection._
import pages.sections.additionalReference.{AdditionalReferenceSection, AdditionalReferencesSection}
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

class UnloadingFindingsAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends UnloadingAnswersHelper(userAnswers) {

  val headerSection: Section = StaticSection(
    rows = Seq(
      traderAtDestinationRow
    )
  )

  def traderAtDestinationRow: SummaryListRow = buildRow(
    prefix = "traderAtDestination",
    answer = userAnswers.ie043Data.TraderAtDestination.identificationNumber.toText,
    id = None,
    call = None
  )

  def buildVehicleNationalityRow(index: Index): Option[SummaryListRow] =
    userAnswers.get(CountryPage(index)).map(transportRegisteredCountry)

  def buildTransportSections: Seq[Section] =
    userAnswers
      .get(TransportMeansListSection)
      .getOrElse(JsArray())
      .zipWithIndex
      .map {
        case (_, index) =>
          AccordionSection(
            sectionTitle = messages("unloadingFindings.subsections.transportMeans", index.display),
            rows = Seq(
              transportMeansID(index),
              transportMeansNumber(index),
              buildVehicleNationalityRow(index)
            ).flatten
          )
      }

  def transportMeansID(transportMeansIndex: Index): Option[SummaryListRow] =
    userAnswers
      .get(TransportMeansIdentificationPage(transportMeansIndex))
      .flatMap(
        identificationAnswer =>
          buildRowWithAnswer[TransportMeansIdentification](
            optionalAnswer = Some(identificationAnswer),
            formatAnswer = formatAsText,
            prefix = "checkYourAnswers.departureMeansOfTransport.identification",
            id = Some(s"change-transport-means-identification-${transportMeansIndex.display}"),
            call = Some(Call(GET, "#"))
          )
      )

  def transportMeansNumber(transportMeansIndex: Index): Option[SummaryListRow] =
    userAnswers
      .get(VehicleIdentificationNumberPage(transportMeansIndex))
      .flatMap(
        identificationNumber =>
          buildRowWithAnswer[String](
            optionalAnswer = Some(identificationNumber),
            formatAnswer = formatAsText,
            prefix = "checkYourAnswers.departureMeansOfTransport.identificationNumber",
            id = Some(s"change-transport-means-identification-number-${transportMeansIndex.display}"),
            call = Some(Call(GET, "#"))
          )
      )

  def transportRegisteredCountry(answer: Country): SummaryListRow = buildSimpleRow(
    answer = Text(answer.description),
    label = messages("unloadingFindings.rowHeadings.vehicleNationality"),
    prefix = "checkYourAnswers.departureMeansOfTransport.country",
    id = Some("change-registered-country"),
    call = Some(Call(GET, "#")),
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
            AccordionSection(
              sectionTitle = messages("unloadingFindings.subsections.transportEquipment", equipmentIndex.display),
              rows = rows
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
    id = Some(s"change-container-identification-number-${index.display}"),
    args = None,
    call = Some(Call(GET, "#"))
  )

  def transportEquipmentSeal(equipmentIndex: Index, sealIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = SealIdentificationNumberPage(equipmentIndex, sealIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.sealIdentifier",
    args = sealIndex.display,
    id = Some(s"change-seal-details-${sealIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  def additionalReference(index: Index): Option[SummaryListRow] = getAnswerAndBuildRow[AdditionalReference](
    page = AdditionalReferenceSection(index),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.additional.reference",
    args = index.display,
    id = Some(s"change-additional-reference-${index.display}"),
    call = Some(Call(GET, "#")) //TODO change me please
  )(AdditionalReference.reads(index))

  def additionalReferences: Seq[SummaryListRow] = getAnswersAndBuildSectionRows(AdditionalReferencesSection)(additionalReference)

  def houseConsignmentSections: Seq[Section] =
    userAnswers.get(HouseConsignmentsSection).mapWithIndex {
      (_, houseConsignmentIndex) =>
        val rows = buildHouseConsignmentRows(
          houseConsignmentTotalWeightRows(houseConsignmentIndex),
          consignorName(houseConsignmentIndex),
          consignorIdentification(houseConsignmentIndex),
          consigneeName(houseConsignmentIndex),
          consigneeIdentification(houseConsignmentIndex)
        )

        Some(
          AccordionSection(
            sectionTitle = messages("unloadingFindings.subsections.houseConsignment", houseConsignmentIndex.display),
            rows = rows,
            viewLink = Link(
              id = s"view-house-consignment-${houseConsignmentIndex.display}",
              href = controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex).url,
              visuallyHidden = messages("summaryDetails.visuallyHidden", houseConsignmentIndex.display)
            ),
            id = s"houseConsignment${houseConsignmentIndex.display}"
          )
        )
    }
}
