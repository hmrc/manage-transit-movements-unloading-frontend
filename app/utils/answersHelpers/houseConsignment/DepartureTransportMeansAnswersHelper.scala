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

package utils.answersHelpers.houseConsignment

import models.departureTransportMeans.TransportMeansIdentification
import models.reference.Country
import models.{Index, UserAnswers}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class DepartureTransportMeansAnswersHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index,
  transportMeansIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def transportMeansID: Option[SummaryListRow] = getAnswerAndBuildRow[TransportMeansIdentification](
    page = DepartureTransportMeansIdentificationTypePage(houseConsignmentIndex, transportMeansIndex),
    formatAnswer = formatAsText,
    prefix = "checkYourAnswers.departureMeansOfTransport.identification",
    id = None,
    call = None
  )

  def transportMeansIDNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = DepartureTransportMeansIdentificationNumberPage(houseConsignmentIndex, transportMeansIndex),
    formatAnswer = formatAsText,
    prefix = "checkYourAnswers.departureMeansOfTransport.identificationNumber",
    id = None,
    call = None
  )

  def buildVehicleNationalityRow: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = DepartureTransportMeansCountryPage(houseConsignmentIndex, transportMeansIndex),
    formatAnswer = x => Text(x.description),
    prefix = "unloadingFindings.rowHeadings.vehicleNationality",
    id = None,
    call = None,
    args = Seq.empty
  )
}
