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

import models.{Index, UserAnswers}
import pages.incident.transportEquipment.IncidentContainerIdentificationNumberPage
import pages.sections.incidents.transportEquipment.{IncidentItemsSection, IncidentSealsSection}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper

class IncidentTransportEquipmentAnswersHelper(
  userAnswers: UserAnswers,
  equipmentIndex: Index,
  incidentIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def containerIdentificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = IncidentContainerIdentificationNumberPage(incidentIndex, equipmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.containerIdentificationNumber",
    id = Some(s"change-container-identification-number-${equipmentIndex.display}"),
    args = None,
    call = Some(Call(GET, "#"))
  )

  def transportEquipmentSeals: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(IncidentSealsSection(incidentIndex, equipmentIndex)) {
      sealIndex =>
        val helper = new IncidentSealAnswersHelper(userAnswers, incidentIndex, equipmentIndex, sealIndex)
        helper.transportEquipmentSeal
    }

  def itemNumber: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(IncidentItemsSection(incidentIndex, equipmentIndex)) {
      itemIndex =>
        val helper = new IncidentItemAnswersHelper(userAnswers, incidentIndex, equipmentIndex, itemIndex)
        helper.transportEquipmentItem
    }

}
