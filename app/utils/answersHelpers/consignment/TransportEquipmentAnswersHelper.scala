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

package utils.answersHelpers.consignment

import models.{CheckMode, Index, Link, NormalMode, RichOptionalJsArray, UserAnswers}
import pages.ContainerIdentificationNumberPage
import pages.sections.SealsSection
import pages.sections.transport.equipment.ItemsSection
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.transportEquipment.{ItemAnswersHelper, SealAnswersHelper}
import viewModels.sections.Section
import viewModels.sections.Section.AccordionSection

class TransportEquipmentAnswersHelper(
  userAnswers: UserAnswers,
  equipmentIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def containerIdentificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ContainerIdentificationNumberPage(equipmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.containerIdentificationNumber",
    id = Some(s"change-container-identification-number-${equipmentIndex.display}"),
    args = equipmentIndex.display,
    call = Some(controllers.transportEquipment.index.routes.ContainerIdentificationNumberController.onPageLoad(arrivalId, equipmentIndex, CheckMode))
  )

  def transportEquipmentSeals: Section =
    userAnswers
      .get(SealsSection(equipmentIndex))
      .mapWithIndex {
        case (_, index) =>
          val helper = new SealAnswersHelper(userAnswers, equipmentIndex, index)
          Seq(helper.transportEquipmentSeal).flatten
      } match {
      case rows =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.seals")),
          rows = rows.flatten,
          id = Some(s"transport-equipment-$equipmentIndex-seals"),
          viewLinks = Seq(sealsAddRemoveLink)
        )
    }

  def transportEquipmentItems: Section =
    userAnswers
      .get(ItemsSection(equipmentIndex))
      .mapWithIndex {
        case (_, index) =>
          val helper = new ItemAnswersHelper(userAnswers, equipmentIndex, index)
          Seq(helper.transportEquipmentItem).flatten
      } match {
      case rows =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.goodsReferences")),
          rows = rows.flatten,
          id = Some(s"transport-equipment-$equipmentIndex-items"),
          viewLinks = Seq(itemsAddRemoveLink)
        )
    }

  private def sealsAddRemoveLink: Link =
    Link(
      id = s"add-remove-transport-equipment-${equipmentIndex.display}-seal",
      href = controllers.transportEquipment.index.routes.AddAnotherSealController.onPageLoad(arrivalId, CheckMode, NormalMode, equipmentIndex).url,
      text = messages("sealsLink.addRemove"),
      visuallyHidden = Some(messages("sealsLink.visuallyHidden", equipmentIndex.display))
    )

  private def itemsAddRemoveLink: Link =
    Link(
      id = s"add-remove-transport-equipment-${equipmentIndex.display}-item",
      href = controllers.transportEquipment.index.routes.ApplyAnotherItemController.onPageLoad(arrivalId, CheckMode, NormalMode, equipmentIndex).url,
      text = messages("consignmentItemLink.addRemove", equipmentIndex.display)
    )
}
