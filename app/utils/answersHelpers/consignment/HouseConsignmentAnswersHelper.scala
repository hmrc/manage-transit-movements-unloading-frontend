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
import models.{Index, Link, RichOptionalJsArray, UserAnswers}
import pages._
import pages.houseConsignment.index.GrossWeightPage
import pages.sections.ItemsSection
import pages.sections.departureTransportMeans.DepartureTransportMeansListSection
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.houseConsignment.{ConsignmentItemAnswersHelper, DepartureTransportMeansAnswersHelper}
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

class HouseConsignmentAnswersHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def headerSection: Section = StaticSection(
    rows = Seq(
      grossMassRow
    ).flatten
  )

  def grossMassRow: Option[SummaryListRow] = getAnswerAndBuildRow[BigDecimal](
    page = GrossWeightPage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.grossMass",
    id = Some(s"change-gross-mass"),
    call = Some(Call(GET, "#"))
  )

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

  def houseConsignmentConsignorSection: Section =
    StaticSection(
      sectionTitle = messages("unloadingFindings.consignor.heading"),
      rows = Seq(
        consignorIdentification,
        consignorName
      ).flatten
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

  def departureTransportMeansSection: Section =
    userAnswers.get(DepartureTransportMeansListSection(houseConsignmentIndex)).mapWithIndex {
      case (_, index) =>
        val helper = new DepartureTransportMeansAnswersHelper(userAnswers, houseConsignmentIndex, index)
        val rows = Seq(
          helper.transportMeansID,
          helper.transportMeansIDNumber,
          helper.buildVehicleNationalityRow
        ).flatten
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportMeans", index.display)),
          rows = rows,
          id = Some(s"departureTransportMeans$index")
        )
    } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportMeans.parent.header")),
          children = children,
          id = Some("departureTransportMeans")
        )
    }

  def itemSection: Section =
    userAnswers.get(ItemsSection(houseConsignmentIndex)).mapWithIndex {
      case (_, index) =>
        val helper = new ConsignmentItemAnswersHelper(userAnswers, houseConsignmentIndex, index)

        val rows = Seq(
          helper.descriptionRow,
          helper.declarationType,
          helper.countryOfDestination,
          Seq(helper.grossWeightRow),
          Seq(helper.netWeightRow),
          helper.cusCodeRow,
          Seq(helper.commodityCodeRow),
          Seq(helper.nomenclatureCodeRow)
        ).flatten

        val children = Seq(
          helper.dangerousGoodsSection,
          helper.itemLevelConsigneeSection,
          helper.documentSection,
          helper.additionalReferencesSection,
          helper.additionalInformationSection,
          helper.packageSection
        )

        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.item", index.display)),
          rows = rows,
          children = children,
          id = Some(s"item-$index")
        )
    } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.item.parent.heading")),
          viewLinks = Seq(itemsAddRemoveLink),
          children = children,
          id = Some("items")
        )
    }

  def itemsAddRemoveLink: Link =
    Link(
      id = "add-remove-items",
      href = "#",
      text = messages("itemsLink.addRemove"),
      visuallyHidden = messages("itemsLink.visuallyHidden")
    )
}
