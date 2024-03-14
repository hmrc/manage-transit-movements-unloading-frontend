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

import models.DocType.Previous
import models.reference.Country
import models.{CheckMode, Index, Link, NormalMode, RichOptionalJsArray, UserAnswers}
import pages.houseConsignment.index.items.document.TypePage
import pages.houseConsignment.index.items.{
  ConsigneeAddressPage => ItemConsigneeAddressPage,
  ConsigneeCountryPage => ItemConsigneeCountryPage,
  ConsigneeIdentifierPage => ItemConsigneeIdentifierPage,
  ConsigneeNamePage => ItemConsigneeNamePage,
  _
}
import pages.sections.PackagingListSection
import pages.sections.houseConsignment.index.items.additionalInformation.AdditionalInformationsSection
import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferencesSection
import pages.sections.houseConsignment.index.items.dangerousGoods.DangerousGoodsListSection
import pages.sections.houseConsignment.index.items.documents.DocumentsSection
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.AnswersHelper
import utils.answersHelpers.consignment.houseConsignment.item._
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
    call = Some(controllers.houseConsignment.index.items.routes.DescriptionController.onPageLoad(arrivalId, CheckMode, houseConsignmentIndex, itemIndex))
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
    change = controllers.houseConsignment.index.items.routes.GrossWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode),
    remove =
      controllers.houseConsignment.index.items.routes.RemoveGrossWeightYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode),
    hiddenLink = "grossWeightLink"
  )

  def netWeightRow: SummaryListRow = getAnswerAndBuildRowWithRemove[Double](
    page = NetWeightPage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.rowHeadings.item.netWeight",
    args = itemIndex.display,
    id = s"net-weight-${itemIndex.display}",
    change = controllers.houseConsignment.index.items.routes.NetWeightController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode),
    remove = Call(GET, "#"),
    hiddenLink = "netWeightLink"
  )

  def additionalReferencesSection: Seq[Section] =
    userAnswers.get(AdditionalReferencesSection(houseConsignmentIndex, itemIndex)).mapWithIndex {
      case (_, index) =>
        val helper = new AdditionalReferencesAnswerHelper(userAnswers, houseConsignmentIndex, itemIndex, index)
        AccordionSection(
          sectionTitle = messages("unloadingFindings.houseConsignment.item.additionalReference", index.display),
          rows = Seq(
            helper.code,
            helper.referenceNumber
          ).flatten
        )
    }

  def additionalInformationsSection: Seq[Section] =
    userAnswers.get(AdditionalInformationsSection(houseConsignmentIndex, itemIndex)).mapWithIndex {
      case (_, index) =>
        val helper = new AdditionalInformationsAnswerHelper(userAnswers, houseConsignmentIndex, itemIndex, index)
        AccordionSection(
          sectionTitle = messages("unloadingFindings.additional.information.heading", index.display),
          rows = Seq(
            helper.additionalInformationCodeRow,
            helper.additionalInformationTextRow
          ).flatten
        )
    }

  def documentSections: Seq[Section] =
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
            sectionTitle = messages("unloadingFindings.houseConsignment.item.document.heading", index.display),
            rows = rows
          )
      }

  def dangerousGoodsRows: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(DangerousGoodsListSection(houseConsignmentIndex, itemIndex)) {
      index =>
        val helper = new DangerousGoodsAnswerHelper(userAnswers, houseConsignmentIndex, itemIndex, index)
        helper.dangerousGoodsRow
    }

  def packageSections: Seq[Section] =
    userAnswers
      .get(PackagingListSection(houseConsignmentIndex, itemIndex))
      .mapWithIndex {
        case (_, index) =>
          val helper = new PackagingAnswersHelper(userAnswers, houseConsignmentIndex, itemIndex, index)

          val rows = Seq(helper.packageTypeRow, helper.packageCountRow, helper.packageMarksRow).flatten

          AccordionSection(
            sectionTitle = messages("unloadingFindings.subsections.packages", index.display),
            rows = rows
          )
      }

  def cusCodeRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.cusCode",
    args = itemIndex.display,
    id = Some(s"change-cus-code-${itemIndex.display}"),
    call = Some(Call(GET, "#"))
  )

  def commodityCodeRow: SummaryListRow = getAnswerAndBuildRowWithRemove[String](
    page = CommodityCodePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.commodityCode",
    args = itemIndex.display,
    id = s"commodity-code-${itemIndex.display}",
    change = controllers.houseConsignment.index.items.routes.CommodityCodeController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, CheckMode),
    remove =
      controllers.houseConsignment.index.items.routes.RemoveCommodityCodeYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, NormalMode),
    hiddenLink = "commodityCodeLink"
  )

  def nomenclatureCodeRow: SummaryListRow = getAnswerAndBuildRowWithRemove[String](
    page = CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex),
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.rowHeadings.item.nomenclatureCode",
    args = itemIndex.display,
    id = s"nomenclature-code-${itemIndex.display}",
    change = Call(GET, "#"),
    remove = Call(GET, "#"),
    hiddenLink = "nomenclatureCodeLink"
  )

  private[consignment] def packagingAddRemoveLink: Link =
    Link(
      id = s"add-remove-packaging",
      href = "#",
      text = messages("packagingLink.addRemove"),
      visuallyHidden = messages("packagingLink.visuallyHidden")
    )

  private[consignment] def documentAddRemoveLink: Link =
    Link(
      id = s"add-remove-document",
      href = "#",
      text = messages("documentLink.addRemove"),
      visuallyHidden = messages("documentLink.visuallyHidden")
    )

  private[consignment] def additionalReferenceAddRemoveLink: Link =
    Link(
      id = s"add-remove-additionalReference",
      href = "#",
      text = messages("additionalReferenceLink.addRemove"),
      visuallyHidden = messages("additionalReferenceLink.visuallyHidden")
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
}
