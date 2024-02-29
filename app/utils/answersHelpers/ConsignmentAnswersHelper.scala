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
import models.reference.CustomsOffice
import models.{Index, Link, SecurityType, UserAnswers}
import pages.documents.TypePage
import pages.grossMass.GrossMassPage
import pages.sections._
import pages.sections.additionalInformation.AdditionalInformationListSection
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
import utils.answersHelpers.consignment.incident.IncidentAnswersHelper
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

class ConsignmentAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def headerSection: Section = StaticSection(
    rows = Seq(
      declarationAcceptanceDateRow,
      declarationTypeRow,
      securityTypeRow,
      Some(reducedDatasetIndicatorRow),
      customsOfficeOfDestinationActual,
      grossMassRow,
      Some(traderAtDestinationRow)
    ).flatten
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
    answer = formatAsBoolean(userAnswers.ie043Data.TransitOperation.reducedDatasetIndicator.toString)
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

  def customsOfficeOfDestinationActual: Option[SummaryListRow] =
    getAnswerAndBuildRow[CustomsOffice](
      page = CustomsOfficeOfDestinationActualPage,
      formatAnswer = x => formatAsText(x.name),
      prefix = "customsOfficeOfDestinationActual",
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

  def departureTransportMeansSection: Section =
    userAnswers
      .get(TransportMeansListSection)
      .mapWithIndex {
        case (_, index) =>
          val helper = new DepartureTransportMeansAnswersHelper(userAnswers, index)
          Seq(
            helper.transportMeansID,
            helper.transportMeansNumber,
            helper.transportRegisteredCountry
          ).flatten
      } match {
      case Nil =>
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportMeans.parent.header")),
          viewLinks = Seq(departureTransportMeansAddRemoveLink)
        )
      case sections =>
        val children = sections.zipWithIndex.map {
          case (rows, index) =>
            AccordionSection(
              sectionTitle = Some(messages("unloadingFindings.subsections.transportMeans", Index(index).display)),
              rows
            )
        }
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportMeans.parent.header")),
          children = children,
          viewLinks = Seq(departureTransportMeansAddRemoveLink),
          id = Some("departureTransportMeans")
        )
    }

  def transportEquipmentSection: Section =
    userAnswers.get(TransportEquipmentListSection).mapWithIndex {
      (_, equipmentIndex) =>
        val helper = new TransportEquipmentAnswersHelper(userAnswers, equipmentIndex)
        Seq(helper.containerIdentificationNumber, helper.transportEquipmentSeals).flatten
    } match {
      case Nil =>
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportEquipment.parent.heading")),
          viewLinks = Seq(transportEquipmentAddRemoveLink)
        )
      case sectionsRows =>
        val transportEquipments = sectionsRows.zipWithIndex.map {
          case (rows, index) =>
            AccordionSection(
              sectionTitle = Some(messages("unloadingFindings.subsections.transportEquipment", Index(index).display)),
              viewLinks = Seq(sealsAddRemoveLink),
              rows = rows
            )
        }
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.transportEquipment.parent.heading")),
          viewLinks = Seq(transportEquipmentAddRemoveLink),
          children = transportEquipments,
          id = Some("transportEquipments")
        )
    }

  def additionalReferencesSection: Section =
    userAnswers.get(AdditionalReferencesSection).mapWithIndex {
      (_, referenceIndex) =>
        val helper = new AdditionalReferenceAnswersHelper(userAnswers, referenceIndex)
        Seq(helper.code, helper.referenceNumber).flatten
    } match {
      case Nil =>
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.additional.reference.heading")),
          viewLinks = Seq(additionalReferenceAddRemoveLink)
        )
      case sectionsRows =>
        val children = sectionsRows.zipWithIndex.map {
          case (rows, index) =>
            AccordionSection(
              sectionTitle = Some(messages("unloadingFindings.additional.reference", Index(index).display)),
              rows = rows
            )
        }
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.additional.reference.heading")),
          viewLinks = Seq(additionalReferenceAddRemoveLink),
          children = children,
          id = Some("additionalReferences")
        )
    }

  def additionalInformationSection: Option[Section] =
    userAnswers.get(AdditionalInformationListSection).mapWithIndex {
      (_, referenceIndex) =>
        val helper = new AdditionalInformationAnswersHelper(userAnswers, referenceIndex)
        Seq(helper.code, helper.description).flatten
    } match {
      case Nil =>
        None
      case sectionsRows =>
        val children = sectionsRows.zipWithIndex.map {
          case (rows, index) =>
            AccordionSection(
              sectionTitle = Some(messages("unloadingFindings.additionalInformation.label", Index(index).display)),
              rows = rows
            )
        }
        Some(
          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.additionalInformation.heading")),
            children = children,
            id = Some("additionalInformation")
          )
        )
    }

  def incidentSection: Option[Section] =
    userAnswers
      .get(IncidentsSection)
      .mapWithIndex {
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

          val transhipment = StaticSection(
            sectionTitle = Some(messages("unloadingFindings.subsections.incidents.transhipment")),
            rows = helper.incidentTranshipment
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
            sectionTitle = Some(messages("unloadingFindings.subsections.incidents", incidentIndex.display)),
            rows = rows,
            children = Seq(endorsementSection) ++ helper.incidentTransportEquipments ++ Seq(transhipment)
          )
      }
      .toList match {
      case Nil => None
      case sections =>
        Some(
          AccordionSection(
            sectionTitle = Some(messages("unloadingFindings.subsections.incidents.parent.header")),
            children = sections,
            id = Some("incidents")
          )
        )
    }

  def documentSection: Section =
    userAnswers.get(DocumentsSection).mapWithIndex {
      case (_, documentIndex) =>
        val helper   = new DocumentAnswersHelper(userAnswers, documentIndex)
        val readOnly = userAnswers.get(TypePage(documentIndex)).map(_.`type`).contains(Previous)

        Seq(
          helper.documentType(readOnly),
          helper.referenceNumber(readOnly),
          helper.additionalInformation(readOnly)
        ).flatten
    } match {
      case Nil =>
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.document.heading.parent.heading")),
          viewLinks = Seq(documentAddRemoveLink)
        )
      case documentSectionRows =>
        val documents = documentSectionRows.zipWithIndex.map {
          case (rows, index) =>
            AccordionSection(
              sectionTitle = Some(messages("unloadingFindings.document.heading", Index(index).display)),
              rows = rows
            )
        }
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.document.heading.parent.heading")),
          viewLinks = Seq(documentAddRemoveLink),
          children = documents,
          id = Some("documents")
        )
    }

  // Don't show children sections here. These are accessed from the 'More details' link
  def houseConsignmentSection: Section =
    userAnswers
      .get(HouseConsignmentsSection)
      .mapWithIndex {
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
            viewLinks = Seq(
              Link(
                id = s"view-house-consignment-${houseConsignmentIndex.display}",
                href = controllers.routes.HouseConsignmentController.onPageLoad(arrivalId, houseConsignmentIndex).url,
                visuallyHidden = messages("summaryDetails.visuallyHidden", houseConsignmentIndex.display)
              )
            ),
            id = s"houseConsignment${houseConsignmentIndex.display}"
          )
      }
      .toList match {
      case Nil =>
        StaticSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.houseConsignment.parent.heading")),
          rows = Nil,
          viewLinks = Nil
        )
      case sections =>
        AccordionSection(
          sectionTitle = Some(messages("unloadingFindings.subsections.houseConsignment.parent.heading")),
          children = sections,
          id = Some("houseConsignments")
        )
    }

  private val documentAddRemoveLink: Link = Link(
    id = s"add-remove-documents",
    href = "#",
    text = messages("documentsLink.addRemove"),
    visuallyHidden = messages("documentsLink.visuallyHidden")
  )

  private val additionalReferenceAddRemoveLink: Link = Link(
    id = "add-remove-additional-reference",
    href = "#",
    text = messages("additionalReferenceLink.addRemove"),
    visuallyHidden = messages("additionalReferenceLink.visuallyHidden")
  )

  private val departureTransportMeansAddRemoveLink: Link =
    Link(
      id = s"add-remove-departure-transport-means",
      href = "#",
      text = messages("departureTransportMeans.addRemove"),
      visuallyHidden = messages("departureTransportMeans.visuallyHidden")
    )

  private val sealsAddRemoveLink: Link =
    Link(
      id = s"add-remove-seals",
      href = "#",
      text = messages("sealsLink.addRemove"),
      visuallyHidden = messages("sealsLink.visuallyHidden")
    )

  private val transportEquipmentAddRemoveLink: Link = Link(
    id = s"add-remove-transport-equipment",
    href = "#",
    text = messages("transportEquipmentLink.addRemove"),
    visuallyHidden = messages("transportEquipmentLink.visuallyHidden")
  )
}
