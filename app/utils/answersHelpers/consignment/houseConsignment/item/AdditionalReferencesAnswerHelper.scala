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

import controllers.houseConsignment.index.items.additionalReference.routes
import models.reference.AdditionalReferenceType
import models.{CheckMode, Index, UserAnswers}
import pages.houseConsignment.index.items.additionalReference._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class AdditionalReferencesAnswerHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index,
  itemIndex: Index,
  additionalReferenceIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def code: Option[SummaryListRow] = getAnswerAndBuildRow[AdditionalReferenceType](
    page = AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, additionalReferenceIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.houseConsignment.item.additionalReference.type",
    id = Some(s"change-additional-reference-type-${itemIndex.display}-${additionalReferenceIndex.display}"),
    call = Some(
      routes.AdditionalReferenceTypeController
        .onPageLoad(arrivalId, CheckMode, CheckMode, CheckMode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
    ),
    args = Seq(itemIndex.display, additionalReferenceIndex.display)*
  )

  def referenceNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, additionalReferenceIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.houseConsignment.item.additionalReference.number",
    id = Some(s"change-additional-reference-number-${itemIndex.display}-${additionalReferenceIndex.display}"),
    call = Some(
      routes.AdditionalReferenceNumberController
        .onPageLoad(arrivalId, CheckMode, CheckMode, CheckMode, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
    ),
    args = Seq(itemIndex.display, additionalReferenceIndex.display)*
  )
}
