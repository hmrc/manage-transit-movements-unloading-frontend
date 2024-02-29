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

import models.reference.AdditionalReferenceType
import models.{Index, UserAnswers}
import pages.additionalReference._
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper

class AdditionalReferenceAnswersHelper(
  userAnswers: UserAnswers,
  referenceIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def code: Option[SummaryListRow] = getAnswerAndBuildRow[AdditionalReferenceType](
    page = AdditionalReferenceTypePage(referenceIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.additional.reference.type",
    id = Some(s"change-additional-reference-type-${referenceIndex.display}"),
    call = Some(Call(GET, "#")),
    args = referenceIndex.display
  )

  def referenceNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = AdditionalReferenceNumberPage(referenceIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.additional.reference.number",
    id = Some(s"change-additional-reference-number-${referenceIndex.display}"),
    call = Some(Call(GET, "#")),
    args = referenceIndex.display
  )
}
