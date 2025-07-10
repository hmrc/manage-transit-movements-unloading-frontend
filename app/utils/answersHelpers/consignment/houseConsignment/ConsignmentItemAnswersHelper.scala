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

import controllers.houseConsignment.index.items.routes
import models.DocType.Previous
import models.reference.Country
import models.{CheckMode, Index, Link, NormalMode, RichOptionalJsArray, UserAnswers}
import pages.houseConsignment.index.items.document.TypePage
import pages.houseConsignment.index.items.{
  ConsigneeAddressPage as ItemConsigneeAddressPage,
  ConsigneeCountryPage as ItemConsigneeCountryPage,
  ConsigneeIdentifierPage as ItemConsigneeIdentifierPage,
  ConsigneeNamePage as ItemConsigneeNamePage,
  *
}
import pages.sections.PackagingListSection
import pages.sections.houseConsignment.index.items.additionalInformation.AdditionalInformationsSection
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferencesSection
import pages.sections.houseConsignment.index.items.dangerousGoods.DangerousGoodsListSection
import pages.sections.houseConsignment.index.items.documents.DocumentsSection
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.houseConsignment.item.*
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

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
    call = Some(routes.DescriptionController.onPageLoad(arrivalId, CheckMode, CheckMode, houseConsignmentIndex, itemIndex)),
    args = itemIndex.display
  )

  def ucrRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = UniqueConsignmentReferencePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.uniqueConsignmentReference",
    id = None,
    call = Some(routes.UniqueConsignmentReferenceController.onPageLoad(arrivalId, CheckMode, CheckMode, houseConsignmentIndex, itemIndex)),
    args = itemIndex.display
  )

  def declarationType: Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = userAnswers.get(DeclarationTypePage(houseConsignmentIndex, itemIndex)),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.declarationType"
  )

  def countryOfDestination: Option[SummaryListRow] = buildRowWithNoChangeLink[Country](
    data = userAnswers.get(CountryOfDestinationPage(houseConsignmentIndex, itemIndex)),
    formatAnswer = formatAsCountry,
    prefix = "unloadingFindings.rowHeadings.item.countryOfDestination"
  )

  def grossWeightRow: SummaryListRow = getAnswerAndBuildRowWithRemove[BigDecimal](
    page = GrossWeightPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.grossWeight",
    args = itemIndex.display,
    id = s"gross-weight-${itemIndex.display}",
    change = routes.GrossWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode, CheckMode),
    remove = routes.RemoveGrossWeightYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode),
    hiddenLink = "grossWeightLink"
  )

  def netWeightRow: SummaryListRow = getAnswerAndBuildRowWithRemove[BigDecimal](
    page = NetWeightPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.netWeight",
    args = itemIndex.display,
    id = s"net-weight-${itemIndex.display}",
    change = controllers.houseConsignment.index.items.routes.NetWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode, CheckMode),
    remove = controllers.houseConsignment.index.items.routes.RemoveNetWeightYesNoController
      .onPageLoad(arrivalId, NormalMode, houseConsignmentIndex, itemIndex),
    hiddenLink = "netWeightLink"
  )

  def additionalReferencesSection: Section =
    userAnswers
      .get(AdditionalReferencesSection(houseConsignmentIndex, itemIndex))
      .mapWithIndex {
        case (_, index) =>
          val helper = new AdditionalReferencesAnswerHelper(userAnswers, houseConsignmentIndex, itemIndex, index)
          val rows   = Seq(helper.code, helper.referenceNumber).flatten
          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.houseConsignment.item.additionalReference", index.display)),
            rows = rows,
            id = Some(s"item-$itemIndex-additional-reference-$index")
          )
      } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.additional.reference.heading")),
          viewLinks = Seq(additionalReferenceAddRemoveLink),
          children = children,
          id = Some(s"item-$itemIndex-additional-references")
        )
    }

  def additionalInformationSection: Section =
    userAnswers
      .get(AdditionalInformationsSection(houseConsignmentIndex, itemIndex))
      .mapWithIndex {
        case (_, index) =>
          val helper = new AdditionalInformationsAnswerHelper(userAnswers, houseConsignmentIndex, itemIndex, index)
          val rows = Seq(
            helper.additionalInformationCodeRow,
            helper.additionalInformationTextRow
          ).flatten
          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.additionalInformation.label", index.display)),
            rows = rows,
            id = Some(s"item-$itemIndex-additional-information-$index")
          )
      } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.additionalInformation.heading")),
          children = children,
          id = Some(s"item-$itemIndex-additional-information")
        )
    }

  def documentSection: Section =
    userAnswers
      .get(DocumentsSection(houseConsignmentIndex, itemIndex))
      .mapWithIndex {
        case (_, index) =>
          val helper   = new DocumentAnswersHelper(userAnswers, houseConsignmentIndex, itemIndex, index)
          val readOnly = userAnswers.get(TypePage(houseConsignmentIndex, itemIndex, index)).map(_.`type`).contains(Previous)

          val rows = Seq(
            helper.documentType(readOnly),
            helper.referenceNumber(readOnly),
            helper.additionalInformation(readOnly)
          ).flatten

          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.document.heading", index.display)),
            rows = rows,
            id = Some(s"item-$itemIndex-document-$index")
          )
      } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.document.heading.parent.heading")),
          viewLinks = Seq(documentAddRemoveLink),
          children = children,
          id = Some(s"item-$itemIndex-documents")
        )
    }

  def dangerousGoodsSection: Section =
    userAnswers.get(DangerousGoodsListSection(houseConsignmentIndex, itemIndex)).mapWithIndex {
      case (_, index) =>
        val helper = new DangerousGoodsAnswerHelper(userAnswers, houseConsignmentIndex, itemIndex, index)
        helper.dangerousGoodsRow
    } match {
      case rows =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.dangerousGoods.unNumbers")),
          rows = rows.flatten,
          id = Some(s"item-$itemIndex-dangerous-goods")
        )
    }

  def packageSection: Section =
    userAnswers
      .get(PackagingListSection(houseConsignmentIndex, itemIndex))
      .mapWithIndex {
        case (_, index) =>
          val helper = new PackagingAnswersHelper(userAnswers, houseConsignmentIndex, itemIndex, index)

          val rows = Seq(helper.packageTypeRow, helper.packageCountRow, helper.packageMarksRow).flatten

          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.subsections.packages", index.display)),
            rows = rows,
            id = Some(s"item-$itemIndex-package-$index")
          )
      } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.packages.parent.heading")),
          viewLinks = Seq(packagingAddRemoveLink),
          children = children,
          id = Some(s"item-$itemIndex-packages")
        )
    }

  def cusCodeRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.cusCode",
    args = itemIndex.display,
    id = Some(s"change-cus-code-${itemIndex.display}"),
    call = Some(routes.CustomsUnionAndStatisticsCodeController.onPageLoad(arrivalId, CheckMode, CheckMode, houseConsignmentIndex, itemIndex))
  )

  def commodityCodeRow: SummaryListRow = getAnswerAndBuildRowWithRemove[String](
    page = CommodityCodePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.commodityCode",
    args = itemIndex.display,
    id = s"commodity-code-${itemIndex.display}",
    change = routes.CommodityCodeController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode, CheckMode),
    remove = routes.RemoveCommodityCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode),
    hiddenLink = "commodityCodeLink"
  )

  def nomenclatureCodeRow: SummaryListRow = getAnswerAndBuildRowWithRemove[String](
    page = CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.nomenclatureCode",
    args = itemIndex.display,
    id = s"nomenclature-code-${itemIndex.display}",
    change = routes.CombinedNomenclatureCodeController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode, CheckMode),
    remove = routes.RemoveCombinedNomenclatureCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode),
    hiddenLink = "nomenclatureCodeLink"
  )

  def itemLevelConsigneeSection: Section =
    StaticSection(
      sectionTitle = messages("unloadingFindings.consignee.heading"),
      rows = Seq(
        consigneeIdentification,
        consigneeName,
        consigneeCountry,
        consigneeAddress
      ).flatten
    )

  def consigneeName: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ItemConsigneeNamePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consigneeName",
    id = None,
    call = None
  )

  def consigneeIdentification: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ItemConsigneeIdentifierPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consigneeIdentifier",
    id = None,
    call = None
  )

  def consigneeCountry: Option[SummaryListRow] = buildRowWithNoChangeLink[Country](
    data = userAnswers.get(ItemConsigneeCountryPage(houseConsignmentIndex, itemIndex)),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consigneeCountry"
  )

  def consigneeAddress: Option[SummaryListRow] = buildRowWithNoChangeLink[String](
    data = userAnswers.get(ItemConsigneeAddressPage(houseConsignmentIndex, itemIndex)).map(_.toString),
    formatAnswer = formatAsHtmlContent,
    prefix = "unloadingFindings.rowHeadings.houseConsignment.consigneeAddress"
  )

  private[consignment] def packagingAddRemoveLink: Link = {
    import controllers.houseConsignment.index.items.packages.routes
    Link(
      id = s"add-remove-item-$itemIndex-packaging",
      href = routes.AddAnotherPackageController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode, CheckMode).url,
      text = messages("packagingLink.addRemove"),
      visuallyHidden = Some(messages("packagingLink.visuallyHidden", itemIndex.display))
    )
  }

  private[consignment] def documentAddRemoveLink: Link = {
    import controllers.houseConsignment.index.items.document.routes
    Link(
      id = s"add-remove-item-$itemIndex-document",
      href = routes.AddAnotherDocumentController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode, CheckMode).url,
      text = messages("documentLink.addRemove"),
      visuallyHidden = Some(messages("documentLink.visuallyHidden", itemIndex.display))
    )
  }

  private[consignment] def additionalReferenceAddRemoveLink: Link = {
    import controllers.houseConsignment.index.items.additionalReference.routes
    Link(
      id = s"add-remove-item-$itemIndex-additional-reference",
      href = routes.AddAnotherAdditionalReferenceController.onPageLoad(arrivalId, CheckMode, CheckMode, houseConsignmentIndex, itemIndex).url,
      text = messages("additionalReferenceLink.addRemove"),
      visuallyHidden = Some(messages("additionalReferenceLink.visuallyHidden", itemIndex.display))
    )
  }
}
