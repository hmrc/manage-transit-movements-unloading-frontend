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

import models.reference.PackageType
import models.{Index, UserAnswers}
import pages.houseConsignment.index.items.packaging.{PackagingCountPage, PackagingMarksPage, PackagingTypePage}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper

class PackagingAnswersHelper(userAnswers: UserAnswers, houseConsignmentIndex: Index, itemIndex: Index, packageIndex: Index)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  private[houseConsignment] def packageTypeRow: Option[SummaryListRow] = getAnswerAndBuildRow[PackageType](
    page = PackagingTypePage(houseConsignmentIndex, itemIndex, packageIndex),
    formatAnswer = formatAsPackage,
    prefix = "unloadingFindings.rowHeadings.item.packageType",
    args = Seq(packageIndex.display, itemIndex.display): _*,
    id = Some(s"change-package-type-${itemIndex.display}-${packageIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  private[houseConsignment] def packageMarksRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = PackagingMarksPage(houseConsignmentIndex, itemIndex, packageIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.packageMarks",
    args = Seq(packageIndex.display, itemIndex.display): _*,
    id = Some(s"change-package-mark-${itemIndex.display}-${packageIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  private[houseConsignment] def packageCountRow: Option[SummaryListRow] = getAnswerAndBuildRow[BigInt](
    page = PackagingCountPage(houseConsignmentIndex, itemIndex, packageIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.packageCount",
    args = Seq(packageIndex.display, itemIndex.display): _*,
    id = Some(s"change-package-count-${itemIndex.display}-${packageIndex.display}"),
    call = Some(Call(GET, "#"))
  )

}
