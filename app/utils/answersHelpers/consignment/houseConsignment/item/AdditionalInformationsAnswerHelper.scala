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

import models.{Index, UserAnswers}
import pages.sections.houseConsignment.index.items.additionalInformation.AdditionalInformationSection
import pages.sections.houseConsignment.index.items.additionalInformation.AdditionalInformationSection.AdditionalInformation
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper

class AdditionalInformationsAnswerHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index,
  itemIndex: Index,
  additionalInformationIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def additionalInformationRow: Option[SummaryListRow] = getAnswerAndBuildRow[AdditionalInformation](
    page = AdditionalInformationSection(houseConsignmentIndex, itemIndex, additionalInformationIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.additional.information",
    args = additionalInformationIndex.display,
    id = Some(s"change-additional-information-${additionalInformationIndex.display}"),
    call = Some(Call(GET, "#")) //TODO change me please
  )(AdditionalInformation.reads(houseConsignmentIndex, itemIndex, additionalInformationIndex))

}
