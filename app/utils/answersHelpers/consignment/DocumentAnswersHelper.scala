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

package utils.answersHelpers.consignment

import models.reference.DocumentType
import models.{CheckMode, Index, UserAnswers}
import pages.documents._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper

class DocumentAnswersHelper(
  userAnswers: UserAnswers,
  documentIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def documentType(readOnly: Boolean = false): Option[SummaryListRow] =
    if (readOnly) {
      buildRowWithNoChangeLink[DocumentType](
        data = userAnswers.get(TypePage(documentIndex)),
        formatAnswer = formatAsText,
        prefix = "unloadingFindings.document.type"
      )
    } else {
      getAnswerAndBuildRow[DocumentType](
        page = TypePage(documentIndex),
        formatAnswer = formatAsText,
        prefix = "unloadingFindings.document.type",
        args = documentIndex.display,
        id = Some(s"change-document-type-${documentIndex.display}"),
        call = Some(controllers.documents.routes.TypeController.onPageLoad(arrivalId, CheckMode, documentIndex))
      )
    }

  def referenceNumber(readOnly: Boolean = false): Option[SummaryListRow] =
    if (readOnly) {
      buildRowWithNoChangeLink[String](
        data = userAnswers.get(DocumentReferenceNumberPage(documentIndex)),
        formatAnswer = formatAsText,
        prefix = "unloadingFindings.document.referenceNumber"
      )
    } else {
      getAnswerAndBuildRow[String](
        page = DocumentReferenceNumberPage(documentIndex),
        formatAnswer = formatAsText,
        prefix = "unloadingFindings.document.referenceNumber",
        args = documentIndex.display,
        id = Some(s"change-document-reference-number-${documentIndex.display}"),
        call = Some(controllers.documents.routes.DocumentReferenceNumberController.onPageLoad(arrivalId, CheckMode, documentIndex))
      )
    }

  def additionalInformation(readOnly: Boolean = false): Option[SummaryListRow] =
    if (readOnly) {
      buildRowWithNoChangeLink[String](
        data = userAnswers.get(AdditionalInformationPage(documentIndex)),
        formatAnswer = formatAsText,
        prefix = "unloadingFindings.document.additionalInformation"
      )
    } else {
      getAnswerAndBuildRow[String](
        page = AdditionalInformationPage(documentIndex),
        formatAnswer = formatAsText,
        prefix = "unloadingFindings.document.additionalInformation",
        args = documentIndex.display,
        id = Some(s"change-document-additional-information-${documentIndex.display}"),
        call = Some(controllers.documents.routes.AdditionalInformationController.onPageLoad(arrivalId, CheckMode, documentIndex))
      )
    }
}
