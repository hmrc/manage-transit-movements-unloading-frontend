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

import models.reference.PackageType
import models.{Index, UserAnswers}
import pages.NetWeightPage
import pages.houseConsignment.index.items.packaging.{PackagingCountPage, PackagingMarksPage, PackagingTypePage}
import pages.houseConsignment.index.items.{GrossWeightPage, ItemDescriptionPage}
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferencesSection
import pages.sections.PackagingSection
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.houseConsignment.item.AdditionalReferencesAnswerHelper
import viewModels.sections.Section
import viewModels.sections.Section.AccordionSection

class ConsignmentItemAnswersHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index,
  itemIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def descriptionRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ItemDescriptionPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.description",
    id = None,
    call = None
  )

  def grossWeightRow: Option[SummaryListRow] = getAnswerAndBuildRow[BigDecimal](
    page = GrossWeightPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.grossWeight",
    args = itemIndex.display,
    id = Some(s"change-gross-weight-${houseConsignmentIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  def netWeightRow: Option[SummaryListRow] = getAnswerAndBuildRow[Double](
    page = NetWeightPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.netWeight",
    args = itemIndex.display,
    id = Some(s"change-net-weight-${houseConsignmentIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  def additionalReferences: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(AdditionalReferencesSection(houseConsignmentIndex, itemIndex)) {
      additionalReferenceIndex =>
        new AdditionalReferencesAnswerHelper(userAnswers, houseConsignmentIndex, itemIndex, additionalReferenceIndex).additionalReferenceRow
    }
}
