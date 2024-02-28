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

package utils.answersHelpers.consignment.transhipment

import generated.TranshipmentType02
import models.departureTransportMeans.TransportMeansIdentification
import models.reference.Country
import models.{Index, UserAnswers}
import pages.incident.transhipment.{IdentificationPage, NationalityPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class TranshipmentAnswersHelper(
  userAnswers: UserAnswers,
  transhipment: Option[TranshipmentType02],
  incidentIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def containerIndicator: Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = transhipment.map(_.containerIndicator.toString),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.transhipment.containerIndicator"
  )

  def typeOfIdentification: Option[SummaryListRow] = getAnswerAndBuildRow[TransportMeansIdentification](
    page = IdentificationPage(incidentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.transhipment.typeOfIdentification",
    args = incidentIndex.display,
    id = None,
    call = None
  )

  def identificationNumber: Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = transhipment.map(_.TransportMeans.identificationNumber),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.transhipment.identificationNumber"
  )

  def nationality: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = NationalityPage(incidentIndex),
    formatAnswer = formatAsCountry,
    prefix = "unloadingFindings.rowHeadings.transhipment.nationality",
    args = incidentIndex.display,
    id = None,
    call = None
  )
}