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

package utils.answersHelpers.consignment.houseConsignment

import models.reference.AdditionalInformationCode
import models.{Index, UserAnswers}
import pages.houseConsignment.index.additionalinformation.{HouseConsignmentAdditionalInformationCodePage, HouseConsignmentAdditionalInformationTextPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class HouseConsignmentAdditionalInformationAnswersHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index,
  additionalInformationIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def code: Option[SummaryListRow] = getAnswerAndBuildRow[AdditionalInformationCode](
    page = HouseConsignmentAdditionalInformationCodePage(houseConsignmentIndex, additionalInformationIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.additionalInformation.type",
    id = None,
    call = None
  )

  def description: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = HouseConsignmentAdditionalInformationTextPage(houseConsignmentIndex, additionalInformationIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.additionalInformation.description",
    id = None,
    call = None
  )
}
