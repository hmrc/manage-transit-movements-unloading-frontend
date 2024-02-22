/*
 * Copyright 2024 HM Revenue & Customs
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

import models.reference.Incident
import models.{Index, UserAnswers}
import pages.incident.{IncidentCodePage, IncidentTextPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class IncidentAnswersHelper(userAnswers: UserAnswers, incidentIndex: Index)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def incidentCodeRow: Option[SummaryListRow] = getAnswerAndBuildRow[Incident](
    page = IncidentCodePage(incidentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.incident.code",
    args = incidentIndex.display,
    id = None,
    call = None
  )

  def incidentDescriptionRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = IncidentTextPage(incidentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.incident.description",
    args = incidentIndex.display,
    id = None,
    call = None
  )

}
