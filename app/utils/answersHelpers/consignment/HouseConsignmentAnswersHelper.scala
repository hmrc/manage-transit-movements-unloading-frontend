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

import models.reference.Country
import models.{Index, Link, UserAnswers}
import models.{Index, RichOptionalJsArray, UserAnswers}
import pages._
import pages.sections.ItemsSection
import pages.sections.departureTransportMeans.DepartureTransportMeansListSection
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.houseConsignment.{ConsignmentItemAnswersHelper, DepartureTransportMeansAnswersHelper}
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

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

  def houseConsignmentConsigneeSection: Section =
    StaticSection(
      sectionTitle = messages("unloadingFindings.consignee.heading"),
      rows = Seq(
        consigneeIdentification,
        consigneeName,
        consigneeCountry,
        consigneeAddress
      ).flatten
    )

  def consigneeCountry: Option[SummaryListRow] = buildRowWithNoChangeLink[Country](
    data = userAnswers.get(ConsigneeCountryPage(houseConsignmentIndex)),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consigneeCountry"
  )

  def consigneeAddress: Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = userAnswers.get(ConsigneeAddressPage(houseConsignmentIndex)).map(_.toString),
    formatAnswer = formatAsHtmlContent,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consigneeAddress"
  )

  def departureTransportMeansSections: Seq[Section] =
    userAnswers.get(DepartureTransportMeansListSection(houseConsignmentIndex)).mapWithIndex {
      case (_, index) =>
        val helper = new DepartureTransportMeansAnswersHelper(userAnswers, houseConsignmentIndex, index)
        AccordionSection(
          sectionTitle = messages("unloadingFindings.subsections.transportMeans", index.display),
          rows = Seq(
            helper.transportMeansID,
            helper.transportMeansIDNumber,
            helper.buildVehicleNationalityRow
          ).flatten
        )
    }

  def itemSections: Seq[Section] =
    userAnswers.get(ItemsSection(houseConsignmentIndex)).mapWithIndex {
      case (_, index) =>
        val helper = new ConsignmentItemAnswersHelper(userAnswers, houseConsignmentIndex, index)
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.item", index.display)),
          rows = Seq(
            helper.descriptionRow,
            helper.declarationType,
            helper.countryOfDestination,
            Seq(helper.grossWeightRow),
            Seq(helper.netWeightRow),
            helper.cusCodeRow,
            Seq(helper.commodityCodeRow),
            Seq(helper.nomenclatureCodeRow),
            helper.dangerousGoodsRows
          ).flatten,
          children = Seq(
            Seq(helper.itemLevelConsigneeSection),
            helper.documentSections,
            helper.additionalReferencesSection,
            helper.packageSections
          ).flatten,
          viewLinks = Seq(
            helper.packagingAddRemoveLink, //TODO move to respective parent section
            helper.documentAddRemoveLink, //TODO move to respective parent section
            helper.additionalReferenceAddRemoveLink //TODO move to respective parent section
          )
        )
    }

  def itemsAddRemoveLink: Link =
    Link(
      id = s"add-remove-items",
      href = "#",
      text = messages("itemsLink.addRemove"),
      visuallyHidden = messages("itemsLink.visuallyHidden")
    )
}
