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
import pages.sections.PackagingSection
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper
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

  private[houseConsignment] def packageTypeRow(packageIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[PackageType](
    page = PackagingTypePage(houseConsignmentIndex, itemIndex, packageIndex),
    formatAnswer = formatAsPackage,
    prefix = "unloadingFindings.rowHeadings.item.packageType",
    args = Seq(packageIndex.display, itemIndex.display): _*,
    id = Some(s"change-package-type-${packageIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  private[houseConsignment] def packageMarksRow(packageIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = PackagingMarksPage(houseConsignmentIndex, itemIndex, packageIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.packageMarks",
    args = Seq(packageIndex.display, itemIndex.display): _*,
    id = Some(s"change-package-mark-${packageIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  private[houseConsignment] def packageCountRow(packageIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[BigInt](
    page = PackagingCountPage(houseConsignmentIndex, itemIndex, packageIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.packageCount",
    args = Seq(packageIndex.display, itemIndex.display): _*,
    id = Some(s"change-package-count-${packageIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  def packageSections: Seq[Section] =
    userAnswers
      .get(PackagingSection(houseConsignmentIndex, itemIndex))
      .mapWithIndex {
        case (_, packageIndex) =>
          val rows = Seq(packageTypeRow(packageIndex), packageCountRow(packageIndex), packageMarksRow(packageIndex)).flatten

          val section = AccordionSection(
            sectionTitle = messages("unloadingFindings.subsections.packages", packageIndex.display),
            rows = rows
          )

          section

      }

  def netWeightRow: Option[SummaryListRow] = getAnswerAndBuildRow[Double](
    page = NetWeightPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.netWeight",
    args = itemIndex.display,
    id = Some(s"change-net-weight-${houseConsignmentIndex.display}"),
    call = Some(Call(GET, "#"))
  )
}
