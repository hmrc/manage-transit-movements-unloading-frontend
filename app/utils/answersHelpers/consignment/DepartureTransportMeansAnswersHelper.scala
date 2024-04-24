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

import models.reference.{Country, TransportMeansIdentification}
import models.{CheckMode, Index, UserAnswers}
import pages.departureMeansOfTransport.{CountryPage, TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class DepartureTransportMeansAnswersHelper(
  userAnswers: UserAnswers,
  transportMeansIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def transportMeansID: Option[SummaryListRow] = getAnswerAndBuildRow[TransportMeansIdentification](
    page = TransportMeansIdentificationPage(transportMeansIndex),
    formatAnswer = formatAsText,
    prefix = "checkYourAnswers.departureMeansOfTransport.identification",
    id = Some(s"change-transport-means-identification-${transportMeansIndex.display}"),
    call = Some(controllers.departureMeansOfTransport.routes.IdentificationController.onPageLoad(arrivalId, transportMeansIndex, CheckMode)),
    args = transportMeansIndex.display
  )

  def transportMeansNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = VehicleIdentificationNumberPage(transportMeansIndex),
    formatAnswer = formatAsText,
    prefix = "checkYourAnswers.departureMeansOfTransport.identificationNumber",
    id = Some(s"change-transport-means-identification-${transportMeansIndex.display}"),
    call = Some(controllers.departureMeansOfTransport.routes.IdentificationNumberController.onPageLoad(arrivalId, transportMeansIndex, CheckMode)),
    args = transportMeansIndex.display
  )

  def transportRegisteredCountry: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryPage(transportMeansIndex),
    formatAnswer = x => Text(x.description),
    prefix = "checkYourAnswers.departureMeansOfTransport.country",
    id = Some("change-registered-country"),
    call = Some(controllers.departureMeansOfTransport.routes.CountryController.onPageLoad(arrivalId, transportMeansIndex, CheckMode)),
    args = transportMeansIndex.display
  )
}
