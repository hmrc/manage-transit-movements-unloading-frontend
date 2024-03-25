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

package utils.answersHelpers.consignment.incident

import generated.TransportEquipmentType07
import models.{Index, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper
import viewModels.sections.Section
import viewModels.sections.Section.AccordionSection

class IncidentTransportEquipmentAnswersHelper(
  userAnswers: UserAnswers,
  transportEquipmentType7: TransportEquipmentType07
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def containerIdentificationNumber: Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = transportEquipmentType7.containerIdentificationNumber,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.containerIdentificationNumber"
  )

  def transportEquipmentSeals: Section =
    transportEquipmentType7.Seal.zipWithIndex.flatMap {
      case (seal, i) =>
        val sealIndex = Index(i)
        buildRowWithNoChangeLink[String](
          data = Option(seal.identifier),
          formatAnswer = formatAsText,
          prefix = "unloadingFindings.rowHeadings.sealIdentifier",
          args = sealIndex.display
        )
    } match {
      case rows =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.incident.transportEquipment.seals.heading")),
          rows = rows
        )
    }

  def itemNumbers: Section =
    transportEquipmentType7.GoodsReference.zipWithIndex.flatMap {
      case (goodsReference, i) =>
        val itemIndex = Index(i)
        buildRowWithNoChangeLink[String](
          data = Option(goodsReference.declarationGoodsItemNumber.toString()),
          formatAnswer = formatAsText,
          prefix = "unloadingFindings.rowHeadings.incident.item",
          args = itemIndex.display
        )
    } match {
      case rows =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.incident.transportEquipment.goodsItemNumbers.heading")),
          rows = rows
        )
    }
}
