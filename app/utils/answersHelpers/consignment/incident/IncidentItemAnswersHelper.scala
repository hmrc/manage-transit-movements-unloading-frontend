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

import models.reference.Item
import models.{Index, UserAnswers}
import pages.incident.transportEquipment.{IncidentItemNumberPage, IncidentSealIdentificationNumberPage}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper

class IncidentItemAnswersHelper(
  userAnswers: UserAnswers,
  incidentIndex: Index,
  equipmentIndex: Index,
  itemIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def transportEquipmentItem: Option[SummaryListRow] = getAnswerAndBuildRow[Item](
    page = IncidentItemNumberPage(incidentIndex, equipmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.sealIdentifier",
    args = itemIndex.display,
    id = Some(s"change-seal-details-${itemIndex.display}"),
    call = Some(Call(GET, "#"))
  )
}
