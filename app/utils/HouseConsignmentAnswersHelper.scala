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
import models.{Index, UserAnswers}
import pages._
import pages.houseConsignment.index.items.ItemDescriptionPage
import pages.sections._
import pages.sections.departureTransportMeans.DepartureTransportMeansListSection
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

class HouseConsignmentAnswersHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index
)(implicit messages: Messages)
    extends UnloadingAnswersHelper(userAnswers) {

  def buildVehicleNationalityRow(transportMeansIndex: Index): Option[SummaryListRow] =
    userAnswers.get(DepartureTransportMeansCountryPage(houseConsignmentIndex, transportMeansIndex)).map(transportRegisteredCountry)

  def buildTransportSections: Seq[Section] =
    userAnswers
      .get(DepartureTransportMeansListSection(houseConsignmentIndex))
      .getOrElse(JsArray())
      .zipWithIndex
      .map {
        case (_, index) =>
          AccordionSection(
            sectionTitle = messages("unloadingFindings.subsections.transportMeans", index.display),
            rows = Seq(
              transportMeansID(index),
              buildVehicleNationalityRow(index)
            ).flatten
          )
      }

  def transportMeansID(transportMeansIndex: Index): Option[SummaryListRow] =
    userAnswers
      .get(DepartureTransportMeansIdentificationTypePage(houseConsignmentIndex, transportMeansIndex))
      .flatMap(
        identificationAnswer =>
          buildRowWithAnswer[TransportMeansIdentification](
            optionalAnswer = Some(identificationAnswer),
            formatAnswer = formatAsText,
            prefix = "checkYourAnswers.departureMeansOfTransport.identification",
            id = None,
            call = None
          )
      )

  def transportRegisteredCountry(answer: Country): SummaryListRow = buildSimpleRow(
    answer = Text(answer.description),
    label = messages("unloadingFindings.rowHeadings.vehicleNationality"),
    prefix = "unloadingFindings.rowHeadings.vehicleNationality",
    id = None,
    call = None,
    args = Seq.empty
  )

  def houseConsignmentSection: Seq[Section] = {

    val rows = buildHouseConsignmentRows(
      houseConsignmentTotalWeightRows(houseConsignmentIndex),
      consignorName(houseConsignmentIndex),
      consignorIdentification(houseConsignmentIndex),
      consigneeName(houseConsignmentIndex),
      consigneeIdentification(houseConsignmentIndex)
    )

    Seq(StaticSection(rows = rows))
  }

  def itemSections: Seq[Section] =
    userAnswers
      .get(ItemsSection(houseConsignmentIndex))
      .mapWithIndex {
        (_, itemIndex) =>
          val itemDescription: Option[SummaryListRow] = itemDescriptionRow(houseConsignmentIndex, itemIndex)
          val grossWeight: Option[SummaryListRow]     = grossWeightRow(houseConsignmentIndex, itemIndex)
          val netWeight: Option[SummaryListRow]       = netWeightRow(houseConsignmentIndex, itemIndex)

          Some(
            AccordionSection(
              sectionTitle = messages("unloadingFindings.subsections.item", itemIndex.display),
              rows = Seq(itemDescription, grossWeight, netWeight).flatten
            )
          )
      }

  def itemDescriptionRow(houseConsignmentIndex: Index, itemIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ItemDescriptionPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.description",
    id = None,
    call = None
  )

}
