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

package utils.answersHelpers.consignment.houseConsignment.item

import models.reference.AdditionalInformationCode
import models.{Index, UserAnswers}
import pages.houseConsignment.index.items.additionalinformation.{HouseConsignmentAdditionalInformationCodePage, HouseConsignmentAdditionalInformationTextPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class AdditionalInformationsAnswerHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index,
  itemIndex: Index,
  additionalInformationIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def additionalInformationCodeRow: Option[SummaryListRow] = getAnswerAndBuildRow[AdditionalInformationCode](
    page = HouseConsignmentAdditionalInformationCodePage(houseConsignmentIndex, itemIndex, additionalInformationIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.additional.information.code",
    id = None,
    call = None
  )

  def additionalInformationTextRow: Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = userAnswers.get(HouseConsignmentAdditionalInformationTextPage(houseConsignmentIndex, itemIndex, additionalInformationIndex)),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.additional.information.description"
  )

}
