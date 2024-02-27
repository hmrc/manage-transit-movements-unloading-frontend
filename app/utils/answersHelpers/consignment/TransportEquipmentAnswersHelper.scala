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

import models.{Index, Link, UserAnswers}
import pages.ContainerIdentificationNumberPage
import pages.sections.SealsSection
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.transportEquipment.SealAnswersHelper

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
    args = None,
    call = Some(Call(GET, "#"))
  )

  def transportEquipmentSeals: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(SealsSection(equipmentIndex)) {
      sealIndex =>
        val helper = new SealAnswersHelper(userAnswers, equipmentIndex, sealIndex)
        helper.transportEquipmentSeal
    }

  def sealsAddRemoveLink: Link =
    Link(
      id = s"add-remove-seals-${equipmentIndex.display}",
      href = "#",
      text = messages("sealsLink.addRemove"),
      visuallyHidden = messages("sealsLink.visuallyHidden", equipmentIndex.display)
    )

  val transportEquipmentAddRemoveLink: Link = Link(
    id = s"add-remove-transport-equipment-${equipmentIndex.display}",
    href = "#",
    text = messages("transportEquipmentLink.addRemove"),
    visuallyHidden = messages("transportEquipmentLink.visuallyHidden", equipmentIndex.display)
  )
}
