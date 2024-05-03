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

import controllers.houseConsignment.index.routes
import models.DocType.Previous
import models.reference.Country
import models.{CheckMode, DynamicAddress, Index, Link, RichOptionalJsArray, SecurityType, UserAnswers}
import pages.houseConsignment.consignor.CountryPage
import pages.houseConsignment.index.{CountryOfDestinationPage, GrossWeightPage, SecurityIndicatorFromExportDeclarationPage}
import pages.sections.ItemsSection
import pages.sections.departureTransportMeans.DepartureTransportMeansListSection
import pages.sections.houseConsignment.index
import pages.sections.houseConsignment.index.additionalInformation.AdditionalInformationListSection
import pages.sections.houseConsignment.index.additionalReference.AdditionalReferenceListSection
import pages.{houseConsignment, _}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.houseConsignment._
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

class HouseConsignmentAnswersHelper(
  userAnswers: UserAnswers,
  houseConsignmentIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def grossMassRow: Option[SummaryListRow] = getAnswerAndBuildRow[BigDecimal](
    page = GrossWeightPage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.grossMass",
    id = Some(s"change-gross-mass"),
    call = Some(routes.GrossWeightController.onPageLoad(arrivalId, houseConsignmentIndex, CheckMode))
  )

  def preGrossMassRow: Option[SummaryListRow] = getAnswerAndBuildRow[BigDecimal](
    page = GrossWeightPage(houseConsignmentIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.grossMass",
    id = Some(s"change-gross-mass"),
    call = None
  )

  def safetyAndSecurityDetails: Option[SummaryListRow] = getAnswerAndBuildRow[SecurityType](
    page = SecurityIndicatorFromExportDeclarationPage(houseConsignmentIndex),
    formatAnswer = x => formatAsText(x.toString),
    prefix = "houseConsignment.securityIndicator",
    id = None,
    call = None
  )

  def countryOfDestination: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryOfDestinationPage(houseConsignmentIndex),
    formatAnswer = formatAsCountry,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.countryOfDestination",
    id = None,
    call = None
  )

  def consignorName(suffix: Option[String] = None): Option[SummaryListRow] = suffix.fold(
    getAnswerAndBuildRow[String](
      page = ConsignorNamePage(houseConsignmentIndex),
      formatAnswer = formatAsText,
      prefix = "unloadingFindings.rowHeadings.houseConsignment.consignorName",
      id = None,
      call = None
    )
  ) {
    suffix =>
      getAnswerAndBuildRow[String](
        page = ConsignorNamePage(houseConsignmentIndex),
        formatAnswer = formatAsText,
        prefix = s"unloadingFindings.rowHeadings.houseConsignment.consignorName.$suffix",
        id = None,
        call = None
      )
  }

  def consignorIdentification(suffix: Option[String] = None): Option[SummaryListRow] = suffix.fold(
    getAnswerAndBuildRow[String](
      page = ConsignorIdentifierPage(houseConsignmentIndex),
      formatAnswer = formatAsText,
      prefix = s"unloadingFindings.rowHeadings.houseConsignment.consignorIdentifier",
      id = None,
      call = None
    )
  ) {
    suffix =>
      getAnswerAndBuildRow[String](
        page = ConsignorIdentifierPage(houseConsignmentIndex),
        formatAnswer = formatAsText,
        prefix = s"unloadingFindings.rowHeadings.houseConsignment.consignorIdentifier.$suffix",
        id = None,
        call = None
      )
  }

  def consignorAddress: Option[SummaryListRow] =
    buildRowWithNoChangeLink[DynamicAddress](
      data = userAnswers.get(ConsignorAddressPage(houseConsignmentIndex)),
      formatAnswer = formatAsHtmlContent,
      prefix = "unloadingFindings.consignor.address"
    )

  def consignorCountry: Option[SummaryListRow] = buildRowWithNoChangeLink[Country](
    data = userAnswers.get(CountryPage(houseConsignmentIndex)),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.consignor.country"
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
        consignorIdentification(),
        consignorName(),
        consignorCountry,
        consignorAddress
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

  private val departureTransportMeansAddRemoveLink: Link = Link(
    id = s"add-remove-departure-transport-means",
    href = "#", // TODO update when controller added
    text = messages("houseConsignment.departureTransportMeans.addRemove")
  )

  private val additionalReferenceAddRemoveLink: Link = Link(
    id = "add-remove-additional-reference",
    href = controllers.houseConsignment.index.additionalReference.routes.AddAnotherAdditionalReferenceController
      .onPageLoad(arrivalId, CheckMode, houseConsignmentIndex)
      .url,
    text = messages("additionalReferenceLink.addRemove")
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
          viewLinks = Seq(departureTransportMeansAddRemoveLink),
          id = Some("departureTransportMeans")
        )
    }

  def documentSection: Section =
    userAnswers
      .get(index.documents.DocumentsSection(houseConsignmentIndex))
      .mapWithIndex {
        case (_, index) =>
          val helper   = new DocumentAnswersHelper(userAnswers, houseConsignmentIndex, index)
          val readOnly = userAnswers.get(houseConsignment.index.documents.TypePage(houseConsignmentIndex, index)).map(_.`type`).contains(Previous)

          val rows = Seq(
            helper.documentType(readOnly),
            helper.referenceNumber(readOnly),
            helper.additionalInformation(readOnly)
          ).flatten

          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.document.heading", index.display)),
            rows = rows,
            id = Some(s"document-$index")
          )
      } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.document.heading.parent.heading")),
          viewLinks = Seq(documentAddRemoveLink),
          children = children,
          id = Some(s"documents")
        )
    }

  def additionalReferencesSection: Section =
    userAnswers.get(AdditionalReferenceListSection(houseConsignmentIndex)).mapWithIndex {
      case (_, index) =>
        val helper = new HouseConsignmentAdditionalReferencesAnswersHelper(userAnswers, houseConsignmentIndex, index)
        val rows = Seq(
          helper.referenceType,
          helper.referenceNumber
        ).flatten
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.houseConsignment.additionalReference", index.display)),
          rows = rows,
          id = Some(s"additionalReference$index")
        )
    } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.houseConsignment.additionalReference.heading")),
          viewLinks = Seq(additionalReferenceAddRemoveLink),
          children = children,
          id = Some("additionalReferences")
        )
    }

  def additionalInformationSection: Section =
    userAnswers.get(AdditionalInformationListSection(houseConsignmentIndex)).mapWithIndex {
      case (_, index) =>
        val helper = new HouseConsignmentAdditionalInformationAnswersHelper(userAnswers, houseConsignmentIndex, index)
        val rows = Seq(
          helper.code,
          helper.description
        ).flatten
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.additionalInformation.label", index.display)),
          rows = rows,
          id = Some(s"additionalInformation$index")
        )
    } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.additionalInformation.heading")),
          children = children,
          id = Some("additionalInformation")
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

  private[consignment] def documentAddRemoveLink: Link =
    Link(
      id = s"add-remove-document",
      href = "#",
      text = messages("documentLink.addRemove")
    )

  def itemsAddRemoveLink: Link =
    Link(
      id = "add-remove-items",
      href = controllers.houseConsignment.index.items.routes.AddAnotherItemController.onPageLoad(arrivalId, houseConsignmentIndex, CheckMode).url,
      text = messages("itemsLink.addRemove")
    )
}
