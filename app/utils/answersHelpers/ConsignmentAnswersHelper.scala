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

package utils.answersHelpers

import models.DocType.Previous
import models.{Index, Link, SecurityType, UserAnswers}
import pages.documents.TypePage
import pages.grossMass.GrossMassPage
import pages.sections._
import pages.sections.additionalReference.AdditionalReferencesSection
import pages.sections.documents.DocumentsSection
import pages.sections.incidents.IncidentsSection
import pages.{CustomsOfficeOfDestinationActualPage, SecurityTypePage}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.answersHelpers.consignment._
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

class ConsignmentAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def headerSection: Section = StaticSection(
    rows = Seq(
      declarationAcceptanceDateRow,
      declarationTypeRow,
      securityTypeRow,
      Some(reducedDatasetIndicatorRow),
      Some(customsOfficeOfDestinationActual),
      grossMassRow,
      Some(traderAtDestinationRow)
    ).flatten
  )

  def declarationTypeRow: Option[SummaryListRow] = userAnswers.ie043Data.TransitOperation.declarationType.map(
    dec =>
      buildRowWithNoChangeLink(
        prefix = "declarationType",
        answer = dec.toText
      )
  )

  def securityTypeRow: Option[SummaryListRow] = getAnswerAndBuildRow[SecurityType](
    page = SecurityTypePage,
    formatAnswer = formatAsText,
    prefix = "securityType",
    id = None,
    call = None
  )

  def reducedDatasetIndicatorRow: SummaryListRow = buildRowWithNoChangeLink(
    prefix = "reducedDatasetIndicator",
    answer = formatAsBoolean(userAnswers.ie043Data.TransitOperation.reducedDatasetIndicator.toString)
  )

  def declarationAcceptanceDateRow: Option[SummaryListRow] = userAnswers.ie043Data.TransitOperation.declarationAcceptanceDate.map(
    dec =>
      buildRowWithNoChangeLink(
        prefix = "declarationAcceptanceDate",
        answer = formatAsDate(dec)
      )
  )

  def traderAtDestinationRow: SummaryListRow = buildRow(
    prefix = "traderAtDestination",
    answer = userAnswers.ie043Data.TraderAtDestination.identificationNumber.toText,
    id = None,
    call = None
  )

  def customsOfficeOfDestinationActual: SummaryListRow = buildRow(
    prefix = "customsOfficeOfDestinationActual",
    answer = userAnswers.get(CustomsOfficeOfDestinationActualPage).get.name.toText,
    id = None,
    call = None
  )

  def holderOfTheTransitProcedureSection: Seq[Section] =
    userAnswers.ie043Data.HolderOfTheTransitProcedure.map {
      hotP =>
        val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.rowHeadings.holderOfTheTransitProcedure.heading")),
          rows = Seq(
            helper.identificationNumber(hotP.identificationNumber),
            helper.name(hotP.name),
            helper.country,
            helper.address(hotP.Address),
            helper.tirHolderIdentificationNumber(hotP.TIRHolderIdentificationNumber)
          ).flatten
        )
    }.toList

  def grossMassRow: Option[SummaryListRow] = getAnswerAndBuildRow[BigDecimal](
    page = GrossMassPage,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.grossMass",
    id = Some(s"change-gross-mass"),
    call = Some(Call(GET, "#"))
  )

  def departureTransportMeansSections: Seq[Section] = {
    val sections = userAnswers.get(TransportMeansListSection).mapWithIndex {
      case (_, index) =>
        val helper = new DepartureTransportMeansAnswersHelper(userAnswers, index)
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportMeans", index.display)),
          rows = Seq(
            helper.transportMeansID,
            helper.transportMeansNumber,
            helper.transportRegisteredCountry
          ).flatten,
          viewLink = Some(helper.departureTransportMeansAddRemoveLink)
        )
    }
    if (sections.nonEmpty) sections
    else {
      val helper = new DepartureTransportMeansAnswersHelper(userAnswers, Index(0))
      Seq(
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportMeans.empty")),
          viewLink = Some(helper.departureTransportMeansAddRemoveLink)
        )
      )
    }
  }

  def transportEquipmentSections: Seq[Section] = {
    val sections = userAnswers.get(TransportEquipmentListSection).mapWithIndex {
      (_, equipmentIndex) =>
        val helper = new TransportEquipmentAnswersHelper(userAnswers, equipmentIndex)
        val rows = Seq(
          Seq(helper.containerIdentificationNumber).flatten,
          helper.transportEquipmentSeals
        ).flatten

        AccordionSection(
          sectionTitle = messages("unloadingFindings.subsections.transportEquipment", equipmentIndex.display),
          rows = rows,
          viewLink = helper.transportEquipmentAddRemoveLink,
          secondViewLink = Some(helper.sealsAddRemoveLink)
        )
    }

    if (sections.nonEmpty) sections
    else {
      val helper = new TransportEquipmentAnswersHelper(userAnswers, Index(0))
      Seq(
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportEquipment.empty")),
          viewLink = Some(helper.transportEquipmentAddRemoveLink)
        )
      )
    }

  }

  def additionalReferencesSections: Seq[Section] = {
    val sections = Seq(
      AccordionSection(
        sectionTitle = Some(messages("unloadingFindings.additional.reference.heading")),
        rows = getAnswersAndBuildSectionRows(AdditionalReferencesSection) {
          referenceIndex =>
            val helper = new AdditionalReferenceAnswersHelper(userAnswers, referenceIndex)
            helper.additionalReference
        },
        viewLink = Some(additionalReferenceAddRemoveLink)
      )
    )
    if (sections.nonEmpty) sections
    else {
      Seq(
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.additional.reference.heading")),
          viewLink = Some(additionalReferenceAddRemoveLink)
        )
      )
    }
  }

  def incidentSections: Seq[Section] =
    userAnswers.get(IncidentsSection).mapWithIndex {
      case (_, incidentIndex) =>
        val helper = new IncidentAnswersHelper(userAnswers, incidentIndex)

        val rows = Seq(
          helper.incidentCountryRow,
          helper.incidentCodeRow,
          helper.incidentDescriptionRow,
          helper.incidentQualifierRow,
          helper.incidentCoordinatesRow,
          helper.incidentUnLocodeRow,
          helper.incidentLocationAddressRow
        ).flatten

        val endorsementSection = StaticSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.incidents.endorsements")),
          rows = Seq(
            helper.incidentEndorsementDateRow,
            helper.incidentEndorsementAuthorityRow,
            helper.incidentEndorsementCountryRow,
            helper.incidentEndorsementPlaceRow
          ).flatten
        )

        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.incidents", incidentIndex.display)),
          rows = rows,
          children = Seq(endorsementSection)
        )
    }

  def documentSections: Seq[Section] = {
    val sections = userAnswers.get(DocumentsSection).mapWithIndex {
      case (_, documentIndex) =>
        val helper   = new DocumentAnswersHelper(userAnswers, documentIndex)
        val readOnly = userAnswers.get(TypePage(documentIndex)).map(_.`type`).contains(Previous)

        val rows = Seq(
          helper.documentType(readOnly),
          helper.referenceNumber(readOnly),
          helper.additionalInformation(readOnly)
        ).flatten

        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.document.heading", documentIndex.display)),
          rows = rows,
          viewLink = Some(helper.documentAddRemoveLink)
        )
    }
    if (sections.nonEmpty) sections
    else {
      val helper = new DocumentAnswersHelper(userAnswers, Index(0))
      Seq(
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.document.heading.empty")),
          viewLink = Some(helper.documentAddRemoveLink)
        )
      )
    }
  }

  // Don't show children sections here. These are accessed from the 'More details' link
  def houseConsignmentSections: Seq[Section] =
    userAnswers.get(HouseConsignmentsSection).mapWithIndex {
      (_, houseConsignmentIndex) =>
        val helper = new HouseConsignmentAnswersHelper(userAnswers, houseConsignmentIndex)
        val rows = Seq(
          helper.consignorName,
          helper.consignorIdentification,
          helper.consigneeName,
          helper.consigneeIdentification,
          helper.consigneeCountry,
          helper.consigneeAddress
        ).flatten

        AccordionSection(
          sectionTitle = messages("unloadingFindings.subsections.houseConsignment", houseConsignmentIndex.display),
          rows = rows,
          viewLink = Link(
            id = s"view-house-consignment-${houseConsignmentIndex.display}",
            href = controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex).url,
            visuallyHidden = messages("summaryDetails.visuallyHidden", houseConsignmentIndex.display)
          ),
          id = s"houseConsignment${houseConsignmentIndex.display}"
        )
    }

  val additionalReferenceAddRemoveLink: Link = Link(
    id = "add-remove-additional-reference",
    href = "#",
    text = messages("additionalReferenceLink.addRemove"),
    visuallyHidden = messages("additionalReferenceLink.visuallyHidden")
  )
}
