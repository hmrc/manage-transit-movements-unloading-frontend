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
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferenceSection
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferenceSection.AdditionalReference
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper

class AdditionalReferencesAnswerHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index,
  itemIndex: Index,
  additionalReferenceIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def additionalReferenceRow: Option[SummaryListRow] = getAnswerAndBuildRowWithoutKey[AdditionalReference](
    page = AdditionalReferenceSection(houseConsignmentIndex, itemIndex, additionalReferenceIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.additional.reference",
    args = additionalReferenceIndex.display,
    id = Some(s"change-additional-reference-${additionalReferenceIndex.display}"),
    call = Some(Call(GET, "#")) //TODO change me please
  )(AdditionalReference.reads(houseConsignmentIndex, itemIndex, additionalReferenceIndex))

}
