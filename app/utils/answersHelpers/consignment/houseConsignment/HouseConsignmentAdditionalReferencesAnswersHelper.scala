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

package utils.answersHelpers.consignment.houseConsignment

import models.reference.AdditionalReferenceType
import models.{Index, UserAnswers}
import pages.houseConsignment.index.additionalReference.{HouseConsignmentAdditionalReferenceNumberPage, HouseConsignmentAdditionalReferenceTypePage}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class HouseConsignmentAdditionalReferencesAnswersHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index,
  additionalReferenceIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def referenceType: Option[SummaryListRow] = getAnswerAndBuildRow[AdditionalReferenceType](
    page = HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, additionalReferenceIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.houseConsignment.additionalReference.type",
    id = Some(s"change-additional-reference-type-${additionalReferenceIndex.display}"),
    call = Some(Call("GET", "#")),
    args = Seq(additionalReferenceIndex.display): _*
  )

  def referenceNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, additionalReferenceIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.houseConsignment.additionalReference.number",
    id = Some(s"change-additional-reference-number-${additionalReferenceIndex.display}"),
    call = Some(Call("GET", "#")),
    args = Seq(additionalReferenceIndex.display): _*
  )
}
