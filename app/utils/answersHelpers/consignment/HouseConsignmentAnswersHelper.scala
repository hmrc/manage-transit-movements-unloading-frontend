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

import models.{Index, UserAnswers}
import pages._
import pages.sections.ItemsSection
import pages.sections.departureTransportMeans.DepartureTransportMeansListSection
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.houseConsignment.{ConsignmentItemAnswersHelper, DepartureTransportMeansAnswersHelper}
import viewModels.sections.Section
import viewModels.sections.Section.AccordionSection

class HouseConsignmentAnswersHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def consignorName: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConsignorNamePage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consignorName",
    id = None,
    call = None
  )

  def consignorIdentification: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConsignorIdentifierPage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consignorIdentifier",
    id = None,
    call = None
  )

  def consigneeName: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConsigneeNamePage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consigneeName",
    id = None,
    call = None
  )

  def consigneeIdentification: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConsigneeIdentifierPage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consigneeIdentifier",
    id = None,
    call = None
  )

  def departureTransportMeansSections: Seq[Section] =
    userAnswers.get(DepartureTransportMeansListSection(houseConsignmentIndex)).mapWithIndex {
      case (_, transportMeansIndex) =>
        val helper = new DepartureTransportMeansAnswersHelper(userAnswers, houseConsignmentIndex, transportMeansIndex)
        AccordionSection(
          sectionTitle = messages("unloadingFindings.subsections.transportMeans", transportMeansIndex.display),
          rows = Seq(
            helper.transportMeansID,
            helper.transportMeansIDNumber,
            helper.buildVehicleNationalityRow
          ).flatten
        )
    }

  def itemSections: Seq[Section] =
    userAnswers.get(ItemsSection(houseConsignmentIndex)).mapWithIndex {
      case (_, itemIndex) =>
        val helper = new ConsignmentItemAnswersHelper(userAnswers, houseConsignmentIndex, itemIndex)
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.item", itemIndex.display)),
          rows = Seq(
            helper.descriptionRow,
            helper.grossWeightRow,
            helper.netWeightRow,
            helper.cusCodeRow,
            helper.commodityCodeRow,
            helper.nomenclatureCodeRow,
            helper.dangerousGoodsRows
          ).flatten,
          children = Seq(
            helper.packageSections,
            helper.documentSections,
            Seq(helper.additionalReferencesSection)
          ).flatten
        )
    }
}
