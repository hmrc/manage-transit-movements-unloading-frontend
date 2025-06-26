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
import models.reference.TransportMode.InlandMode
import models.reference.{Country, CustomsOffice, SecurityType}
import models.{Link, NormalMode, RichOptionalJsArray, UserAnswers}
import pages.countryOfDestination.CountryOfDestinationPage
import pages.documents.TypePage
import pages.inlandModeOfTransport.InlandModeOfTransportPage
import pages.sections._
import pages.sections.additionalInformation.AdditionalInformationListSection
import pages.sections.additionalReference.AdditionalReferencesSection
import pages.sections.documents.DocumentsSection
import pages.sections.incidents.IncidentsSection
import pages.{CustomsOfficeOfDestinationActualPage, GrossWeightPage, SecurityTypePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.consignment._
import utils.answersHelpers.consignment.incident.IncidentAnswersHelper
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

class ConsignmentAnswersHelper(
  userAnswers: UserAnswers
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  private val documentAddRemoveLink: Link = Link(
    id = s"add-remove-documents",
    href = controllers.documents.routes.AddAnotherDocumentController.onPageLoad(arrivalId, NormalMode).url,
    text = messages("documentsLink.addRemove")
  )

  private val additionalReferenceAddRemoveLink: Link = Link(
    id = "add-remove-additional-reference",
    href = controllers.additionalReference.index.routes.AddAnotherAdditionalReferenceController.onPageLoad(arrivalId, NormalMode).url,
    text = messages("additionalReferenceLink.addRemove")
  )

  private val transportEquipmentAddRemoveLink: Link = Link(
    id = s"add-remove-transport-equipment",
    href = controllers.transportEquipment.routes.AddAnotherEquipmentController.onPageLoad(arrivalId, NormalMode).url,
    text = messages("transportEquipmentLink.addRemove")
  )

  private val departureTransportMeansAddRemoveLink: Link =
    Link(
      id = s"add-remove-departure-transport-means",
      href = controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onPageLoad(arrivalId, NormalMode).url,
      text = messages("departureTransportMeans.addRemove")
    )

  private val houseConsignmentAddRemoveLink: Link =
    Link(
      id = s"add-remove-house-consignment",
      href = controllers.houseConsignment.routes.AddAnotherHouseConsignmentController.onPageLoad(arrivalId, NormalMode).url,
      text = messages("houseConsignment.addRemove")
    )

  def headerSection: Section = StaticSection(
    rows = Seq(
      declarationAcceptanceDateRow,
      declarationTypeRow,
      securityTypeRow,
      Some(reducedDatasetIndicatorRow),
      countryOfDestinationRow,
      customsOfficeOfDestinationActual,
      grossMassRow,
      Some(traderAtDestinationRow)
    ).flatten
  )

  def inlandModeOfTransportSection: Section = StaticSection(
    sectionTitle = messages("unloadingFindings.inlandModeOfTransport"),
    rows = Seq(inlandModeOfTransportRow).flatten
  )

  private def declarationTypeRow: Option[SummaryListRow] = userAnswers.ie043Data.TransitOperation.declarationType.map(
    dec =>
      buildRowWithNoChangeLink(
        prefix = "declarationType",
        answer = dec.toText
      )
  )

  private def securityTypeRow: Option[SummaryListRow] = getAnswerAndBuildRow[SecurityType](
    page = SecurityTypePage,
    formatAnswer = formatAsText,
    prefix = "securityType",
    id = None,
    call = None
  )

  private def reducedDatasetIndicatorRow: SummaryListRow = buildRowWithNoChangeLink(
    prefix = "reducedDatasetIndicator",
    answer = formatAsYesOrNo(userAnswers.ie043Data.TransitOperation.reducedDatasetIndicator)
  )

  private def declarationAcceptanceDateRow: Option[SummaryListRow] = userAnswers.ie043Data.TransitOperation.declarationAcceptanceDate.map(
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

  def customsOfficeOfDestinationActual: Option[SummaryListRow] =
    getAnswerAndBuildRow[CustomsOffice](
      page = CustomsOfficeOfDestinationActualPage,
      formatAnswer = x => formatAsText(x.name),
      prefix = "customsOfficeOfDestinationActual",
      id = None,
      call = None
    )

  def grossMassRow: Option[SummaryListRow] = getAnswerAndBuildRow[BigDecimal](
    page = GrossWeightPage,
    formatAnswer = formatAsWeight,
    prefix = "unloadingFindings.grossMass",
    id = Some(s"change-gross-mass"),
    call = Some(controllers.routes.GrossWeightController.onPageLoad(userAnswers.id))
  )

  def inlandModeOfTransportRow: Option[SummaryListRow] = getAnswerAndBuildRow[InlandMode](
    page = InlandModeOfTransportPage,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.inlandModeOfTransport.label",
    id = None,
    call = None
  )

  def countryOfDestinationRow: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryOfDestinationPage,
    formatAnswer = formatAsText,
    prefix = "unloadingFindings.countryOfDestination",
    id = None,
    call = None
  )

  def consignorSection: Option[Section] =
    userAnswers.ie043Data.Consignment.flatMap(_.Consignor).map {
      consignor =>
        val helper = new ConsignorAnswersHelper(userAnswers)
        StaticSection(
          sectionTitle = messages("unloadingFindings.consignor.heading"),
          rows = Seq(
            helper.identificationNumber(consignor.identificationNumber),
            helper.name(consignor.name),
            helper.country,
            helper.address(consignor.Address)
          ).flatten
        )
    }

  def consigneeSection: Option[Section] =
    userAnswers.ie043Data.Consignment.flatMap(_.Consignee).map {
      consignee =>
        val helper = new ConsigneeAnswersHelper(userAnswers)
        StaticSection(
          sectionTitle = messages("unloadingFindings.consignee.heading"),
          rows = Seq(
            helper.identificationNumber(consignee.identificationNumber),
            helper.name(consignee.name),
            helper.country,
            helper.address(consignee.Address)
          ).flatten
        )
    }

  def holderOfTheTransitProcedureSection: Option[Section] =
    userAnswers.ie043Data.HolderOfTheTransitProcedure.map {
      hotP =>
        val helper = new HolderOfTheTransitProcedureHelper(userAnswers)
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.rowHeadings.holderOfTheTransitProcedure.heading")),
          rows = Seq(
            helper.identificationNumber(hotP.identificationNumber),
            helper.name(hotP.name),
            helper.country,
            helper.address(hotP.Address),
            helper.tirHolderIdentificationNumber(hotP.TIRHolderIdentificationNumber)
          ).flatten
        )
    }

  def departureTransportMeansSection: Section =
    userAnswers
      .get(TransportMeansListSection)
      .mapWithIndex {
        case (_, index) =>
          val helper = new DepartureTransportMeansAnswersHelper(userAnswers, index)
          val rows = Seq(
            helper.transportMeansID,
            helper.transportMeansNumber,
            helper.transportRegisteredCountry
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

  def transportEquipmentSection: Section =
    userAnswers.get(TransportEquipmentListSection).mapWithIndex {
      case (_, index) =>
        val helper = new TransportEquipmentAnswersHelper(userAnswers, index)
        val rows   = Seq(helper.containerIndicatorRow, helper.containerIdentificationNumber).flatten
        val children = Seq(
          helper.transportEquipmentSeals,
          helper.transportEquipmentItems
        )
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportEquipment", index.display)),
          viewLinks = Nil,
          rows = rows,
          children = children,
          id = Some(s"transportEquipment$index")
        )
    } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportEquipment.parent.heading")),
          viewLinks = Seq(transportEquipmentAddRemoveLink),
          children = children,
          id = Some("transportEquipments")
        )
    }

  def additionalReferencesSection: Section =
    userAnswers.get(AdditionalReferencesSection).mapWithIndex {
      case (_, index) =>
        val helper = new AdditionalReferenceAnswersHelper(userAnswers, index)
        val rows   = Seq(helper.code, helper.referenceNumber).flatten
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.additional.reference", index.display)),
          rows = rows,
          id = Some(s"additionalReference$index")
        )
    } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.additional.reference.heading")),
          viewLinks = Seq(additionalReferenceAddRemoveLink),
          children = children,
          id = Some("additionalReferences")
        )
    }

  def additionalInformationSection: Section =
    userAnswers
      .get(AdditionalInformationListSection)
      .mapWithIndex {
        case (_, index) =>
          val helper = new AdditionalInformationAnswersHelper(userAnswers, index)
          val rows   = Seq(helper.code, helper.description).flatten
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

  def incidentSection: Section =
    userAnswers
      .get(IncidentsSection)
      .mapWithIndex {
        case (_, index) =>
          val helper = new IncidentAnswersHelper(userAnswers, index)

          val rows = Seq(
            helper.incidentCountryRow,
            helper.incidentCodeRow,
            helper.incidentDescriptionRow,
            helper.incidentQualifierRow,
            helper.incidentCoordinatesRow,
            helper.incidentUnLocodeRow,
            helper.incidentLocationAddressRow,
            helper.containerIndicator
          ).flatten

          val transhipment = StaticSection(
            sectionTitle = Some(messages("unloadingFindings.subsections.incidents.replacementMeansOfTransport")),
            rows = helper.incidentReplacementMeansOfTransport
          )
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
            sectionTitle = Some(messages("unloadingFindings.subsections.incidents", index.display)),
            rows = rows,
            children = Seq(
              endorsementSection,
              helper.incidentTransportEquipments,
              transhipment
            ),
            id = Some(s"incident$index")
          )
      } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.incidents.parent.header")),
          children = children,
          id = Some("incidents")
        )
    }

  def documentSection: Section =
    userAnswers.get(DocumentsSection).mapWithIndex {
      case (_, index) =>
        val helper   = new DocumentAnswersHelper(userAnswers, index)
        val readOnly = userAnswers.get(TypePage(index)).map(_.`type`).contains(Previous)

        val rows = Seq(
          helper.documentType(readOnly),
          helper.referenceNumber(readOnly),
          helper.additionalInformation(readOnly)
        ).flatten

        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.document.heading", index.display)),
          rows = rows,
          id = Some(s"document$index")
        )
    } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.document.heading.parent.heading")),
          viewLinks = Seq(documentAddRemoveLink),
          children = children,
          id = Some("documents")
        )
    }

  // Don't show children sections here. These are accessed from the 'More details' link
  def houseConsignmentSection: Section =
    userAnswers
      .get(HouseConsignmentsSection)
      .mapWithIndex {
        case (_, index) =>
          val helper = new HouseConsignmentAnswersHelper(userAnswers, index)

          AccordionSection(
            sectionTitle = messages("unloadingFindings.subsections.houseConsignment", index.display),
            rows = Seq(
              helper.grossMassRowOnConsignmentPage,
              helper.consignorIdentificationOnConsignmentPage,
              helper.consignorNameOnConsignmentPage
            ).flatten,
            viewLinks = Seq(
              Link(
                id = s"view-house-consignment-${index.display}",
                text = messages("summaryDetails.link"),
                href = controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, index).url,
                visuallyHidden = Some(messages("summaryDetails.visuallyHidden", index.display))
              )
            ),
            id = s"houseConsignment$index",
            children = Nil
          )
      } match {
      case children =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.houseConsignment.parent.heading")),
          children = children,
          viewLinks = Seq(houseConsignmentAddRemoveLink),
          id = Some("houseConsignments")
        )
    }

}
