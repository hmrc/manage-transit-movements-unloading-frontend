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

import models.{CheckMode, Index, UserAnswers}
import pages.ContainerIdentificationNumberPage
import pages.sections.SealsSection
import pages.sections.transport.equipment.ItemsSection
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.transportEquipment.{ItemAnswersHelper, SealAnswersHelper}

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

  def transportEquipmentSeals: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(SealsSection(equipmentIndex)) {
      index =>
        val helper = new SealAnswersHelper(userAnswers, equipmentIndex, index)
        helper.transportEquipmentSeal
    }

  def transportEquipmentItems: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(ItemsSection(equipmentIndex)) {
      index =>
        val helper = new ItemAnswersHelper(userAnswers, equipmentIndex, index)
        helper.transportEquipmentItem
    }
}
