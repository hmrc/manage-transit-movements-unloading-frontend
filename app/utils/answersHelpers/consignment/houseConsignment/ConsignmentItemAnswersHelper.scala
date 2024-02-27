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

import models.{Index, UserAnswers}
import pages.NetWeightPage
import pages.houseConsignment.index.items._
import pages.sections.PackagingListSection
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferencesSection
import pages.sections.houseConsignment.index.items.dangerousGoods.DangerousGoodsListSection
import pages.sections.houseConsignment.index.items.documents.DocumentsSection
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.houseConsignment.item.{
  AdditionalReferencesAnswerHelper,
  DangerousGoodsAnswerHelper,
  DocumentAnswersHelper,
  PackagingAnswersHelper
}
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

  def additionalReferencesSection: Section = {
    val rows = getAnswersAndBuildSectionRows(AdditionalReferencesSection(houseConsignmentIndex, itemIndex)) {
      additionalReferenceIndex =>
        val helper = new AdditionalReferencesAnswerHelper(userAnswers, houseConsignmentIndex, itemIndex, additionalReferenceIndex)
        helper.additionalReferenceRow
    }

    AccordionSection(
      sectionTitle = messages("unloadingFindings.additional.reference.heading"),
      rows = rows
    )
  }

  def documentSections: Seq[Section] =
    userAnswers
      .get(DocumentsSection(houseConsignmentIndex, itemIndex))
      .mapWithIndex {
        case (_, documentIndex) =>
          val helper = new DocumentAnswersHelper(userAnswers, houseConsignmentIndex, itemIndex, documentIndex)

          val rows = Seq(
            helper.referenceNumber,
            helper.additionalInformation
          ).flatten

          AccordionSection(
            sectionTitle = messages("unloadingFindings.houseConsignment.item.document.heading", documentIndex.display),
            rows = rows
          )
      }

  def dangerousGoodsRows: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(DangerousGoodsListSection(houseConsignmentIndex, itemIndex)) {
      dangerousGoodsIndex =>
        new DangerousGoodsAnswerHelper(userAnswers, houseConsignmentIndex, itemIndex, dangerousGoodsIndex).dangerousGoodsRow
    }

  def packageSections: Seq[Section] =
    userAnswers
      .get(PackagingListSection(houseConsignmentIndex, itemIndex))
      .mapWithIndex {
        case (_, packageIndex) =>
          val helper = new PackagingAnswersHelper(userAnswers, houseConsignmentIndex, itemIndex, packageIndex)

          val rows = Seq(helper.packageTypeRow, helper.packageCountRow, helper.packageMarksRow).flatten

          AccordionSection(
            sectionTitle = messages("unloadingFindings.subsections.packages", packageIndex.display),
            rows = rows
          )
      }

  def cusCodeRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.cusCode",
    args = itemIndex.display,
    id = Some(s"change-cus-code-${houseConsignmentIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  def commodityCodeRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = CommodityCodePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.commodityCode",
    args = itemIndex.display,
    id = Some(s"change-commodity-code-${houseConsignmentIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  def nomenclatureCodeRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.nomenclatureCode",
    args = itemIndex.display,
    id = Some(s"change-nomenclature-code-${houseConsignmentIndex.display}"),
    call = Some(Call(GET, "#"))
  )
}
