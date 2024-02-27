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

import models.reference.DocumentType
import models.{Index, UserAnswers}
import pages.houseConsignment.index.items.document._
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper

class DocumentAnswersHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index,
  itemIndex: Index,
  documentIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def documentType: Option[SummaryListRow] =
    getAnswerAndBuildRow[DocumentType](
      page = TypePage(houseConsignmentIndex, itemIndex, documentIndex),
      formatAnswer = formatAsText,
      prefix = "unloadingFindings.houseConsignment.item.document.type",
      args = Seq(documentIndex.display, itemIndex.display): _*,
      id = Some(s"change-document-type-${itemIndex.display}-${documentIndex.display}"),
      call = Some(Call(GET, "#")) //TODO change me please
    )

  def referenceNumber: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex),
      formatAnswer = formatAsText,
      prefix = "unloadingFindings.houseConsignment.item.document.referenceNumber",
      args = Seq(documentIndex.display, itemIndex.display): _*,
      id = Some(s"change-document-reference-number-${itemIndex.display}-${documentIndex.display}"),
      call = Some(Call(GET, "#")) //TODO change me please
    )

  def additionalInformation: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex),
      formatAnswer = formatAsText,
      prefix = "unloadingFindings.houseConsignment.item.document.additionalInformation",
      args = Seq(documentIndex.display, itemIndex.display): _*,
      id = Some(s"change-document-additional-information-${itemIndex.display}-${documentIndex.display}"),
      call = Some(Call(GET, "#")) //TODO change me please
    )
}
